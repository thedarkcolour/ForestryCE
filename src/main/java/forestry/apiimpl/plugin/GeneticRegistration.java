package forestry.apiimpl.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forestry.api.genetics.ForestryTaxa;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.ITaxon;
import forestry.api.genetics.TaxonomicRank;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.api.plugin.IGeneticRegistration;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.api.plugin.ISpeciesTypeFactory;
import forestry.api.plugin.ITaxonBuilder;
import forestry.core.genetics.Taxon;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

// Handles registration of species types, taxonomy, and filter rules.
public final class GeneticRegistration implements IGeneticRegistration {
	// Already defined taxa that have known parents
	private final HashMap<String, TaxonBuilder> taxaByName = new HashMap<>();
	// Taxa that are waiting for their parents to be registered (Unknown parent name -> child name)
	private final HashMap<String, HashSet<String>> unknownTaxa = new HashMap<>();
	// Name of taxon with missing parent -> Action
	private final HashMap<String, Consumer<ITaxonBuilder>> unknownActions = new HashMap<>();
	// Species type builders
	private final ModifiableRegistrar<ResourceLocation, ISpeciesTypeBuilder, SpeciesTypeBuilder> speciesTypes = new ModifiableRegistrar<>(ISpeciesTypeBuilder.class);
	// Filter rule types used by IFilterRegistry
	private final ArrayList<IFilterRuleType> ruleTypes = new ArrayList<>();

	public GeneticRegistration() {
		// Register default Domain and Kingdom taxa according to seven kingdoms
		TaxonBuilder prokaryota = registerTaxon(TaxonomicRank.DOMAIN, ForestryTaxa.DOMAIN_PROKARYOTA);
		prokaryota.defineSubTaxon(ForestryTaxa.KINGDOM_ARCHAEA);
		prokaryota.defineSubTaxon(ForestryTaxa.KINGDOM_BACTERIA);

		TaxonBuilder eukaryota = registerTaxon(TaxonomicRank.DOMAIN, ForestryTaxa.DOMAIN_EUKARYOTA);
		eukaryota.defineSubTaxon(ForestryTaxa.KINGDOM_FUNGI);
		eukaryota.defineSubTaxon(ForestryTaxa.KINGDOM_PLANT);
		eukaryota.defineSubTaxon(ForestryTaxa.KINGDOM_ANIMAL, animalia -> {
			animalia.defineSubTaxon(ForestryTaxa.PHYLUM_ARTHROPODS, arthropoda -> {
				arthropoda.defineSubTaxon(ForestryTaxa.CLASS_INSECTS);
			});
		});
		eukaryota.defineSubTaxon(ForestryTaxa.KINGDOM_PROTOZOA);
		eukaryota.defineSubTaxon(ForestryTaxa.KINGDOM_CHROMISTA);
	}

	@Override
	public void defineTaxon(String parent, String name) {
		if (this.taxaByName.containsKey(parent)) {
			// immediately define it if the parent is known
			this.taxaByName.get(parent).defineSubTaxon(name);
		} else {
			// defer if parent is not known, keeping track of which taxa are dependent upon this parent
			this.unknownTaxa.computeIfAbsent(parent, key -> new HashSet<>()).add(name);
		}
	}

	@Override
	public void defineTaxon(String parent, String name, Consumer<ITaxonBuilder> action) {
		if (this.taxaByName.containsKey(parent)) {
			this.taxaByName.get(parent).defineSubTaxon(name, action);
		} else {
			this.unknownTaxa.computeIfAbsent(parent, key -> new HashSet<>()).add(name);
			// also defer configuration of the taxon until it can be created
			this.unknownActions.put(name, action);
		}
	}

	@Override
	public void modifySpeciesType(ResourceLocation id, Consumer<ISpeciesTypeBuilder> action) {
		this.speciesTypes.modify(id, action);
	}

	public ImmutableMap<ResourceLocation, ISpeciesType<?, ?>> buildSpeciesTypes() {
		return this.speciesTypes.build(SpeciesTypeBuilder::build);
	}

	// Creates a new taxon builder and puts it in the registry, or returns the existing one if it is already registered
	private TaxonBuilder registerTaxon(TaxonomicRank rank, String name) {
		if (this.taxaByName.containsKey(name)) {
			TaxonBuilder existing = this.taxaByName.get(name);

			if (existing.rank == rank) {
				// registering the same taxon twice is fine, just return the existing one
				return existing;
			} else {
				// registering different taxa with the same name is not fine, names must be unique
				throw new RuntimeException("A taxon with name '" + name + "' is already defined in rank " + existing + " but plugin tried to set it in rank " + rank);
			}
		} else {
			// create and register a new taxon builder
			TaxonBuilder builder = new TaxonBuilder(this, rank, name);
			this.taxaByName.put(name, builder);

			// if any previously registered taxa list this taxon as an "unknown" parent, create and register them too.
			HashSet<String> dependents = this.unknownTaxa.remove(name);
			if (dependents != null) {
				for (String childName : dependents) {
					Consumer<ITaxonBuilder> action = this.unknownActions.remove(childName);
					// recursive call to registerTaxon: registers the taxa that listed these as unknown as well
					if (action != null) {
						builder.defineSubTaxon(name, action);
					} else {
						builder.defineSubTaxon(name);
					}
				}
			}

			return builder;
		}
	}

	@Override
	public void registerFilterRuleType(IFilterRuleType ruleType) {
		this.ruleTypes.add(ruleType);
	}

	// why did i over engineer such an insignificant mechanic
	public ImmutableMap<String, ITaxon> buildTaxa() {
		if (!this.unknownTaxa.isEmpty()) {
			StringBuilder msg = new StringBuilder("The following taxa were not registered, but are parents of registered taxa: ");
			this.unknownTaxa.forEach((parent, children) -> {
				msg.append("\n'").append(parent).append("' is needed by registered subtaxa: ").append(Arrays.toString(children.toArray()));
			});
			throw new IllegalStateException(msg.toString());
		}

		// sort builders by taxonomic rank (domain first, then kingdom, then phyla, etc.) so that parents are created before children
		LinkedHashMap<String, Taxon> taxa = new LinkedHashMap<>(this.taxaByName.size());
		TaxonBuilder[] builders = this.taxaByName.values().toArray(new TaxonBuilder[0]);
		Arrays.sort(builders, Comparator.comparing(taxon -> taxon.rank));
		// keep track of child lists because Taxon.setChildren can only be called once (and children are created after parents)
		IdentityHashMap<Taxon, ImmutableList.Builder<ITaxon>> parentChildrenMap = new IdentityHashMap<>(this.taxaByName.size());

		// traverse taxa from parents down to children so that parent is available in taxa map
		for (TaxonBuilder builder : builders) {
			String name = builder.name;
			TaxonomicRank rank = builder.rank;
			// parents are created before children so taxa should always contain the parent Taxon
			Taxon parent = builder.parent == null ? null : taxa.get(builder.parent.name);
			IdentityHashMap<IChromosome<?>, ITaxon.TaxonAllele> alleles = builder.alleles;

			// create taxon
			Taxon taxon = new Taxon(name, rank, parent, alleles);
			taxa.put(name, taxon);

			// create parent list if there are children, otherwise set to null (empty taxon, genera)
			int childrenCount = builder.children.size();
			if (childrenCount > 0) {
				parentChildrenMap.put(taxon, ImmutableList.builderWithExpectedSize(childrenCount));
			} else {
				taxon.setChildren(List.of());
			}

			// add to parent's list of children
			if (parent != null) {
				// need to use builder's child list for size because Taxon form has no list yet. add as a child
				parentChildrenMap.get(parent).add(taxon);
			}
		}

		// Setup taxa children
		for (Map.Entry<Taxon, ImmutableList.Builder<ITaxon>> entry : parentChildrenMap.entrySet()) {
			entry.getKey().setChildren(entry.getValue().build());
		}

		return ImmutableMap.copyOf(taxa);
	}

	public ArrayList<IFilterRuleType> getFilterRuleTypes() {
		return this.ruleTypes;
	}

	private static class TaxonBuilder implements ITaxonBuilder {
		private final GeneticRegistration registration;
		private final TaxonomicRank rank;
		private final String name;
		private final HashSet<TaxonBuilder> children = new HashSet<>();
		private final IdentityHashMap<IChromosome<?>, ITaxon.TaxonAllele> alleles = new IdentityHashMap<>();
		// Must not be null by end of registration unless this is a domain
		@Nullable
		private TaxonBuilder parent;

		private TaxonBuilder(GeneticRegistration registration, TaxonomicRank rank, String name) {
			this.registration = registration;
			this.rank = rank;
			this.name = name;
		}

		@Override
		public void defineSubTaxon(String name) {
			registerChild(name);
		}

		@Override
		public void defineSubTaxon(String name, Consumer<ITaxonBuilder> action) {
			action.accept(registerChild(name));
		}

		private TaxonBuilder registerChild(String name) {
			if (this.rank == TaxonomicRank.GENUS) {
				throw new UnsupportedOperationException("Cannot directly add species '" + name + "' as a child of the '" + this.name + "' genus. Genera are populated by the ISpeciesBuilder");
			}

			// defining the same child twice is fine...
			TaxonBuilder existing = this.registration.taxaByName.get(name);
			if (existing != null && existing.parent != this) {
				// ...but not when the child already has a parent somewhere else
				String parentName = existing.parent == null ? "null" : existing.parent.name;
				throw new IllegalStateException("Tried to set a taxon with name '" + name + "' as child of taxon '" + this.name + "' but that taxon is already a child of a taxon named'" + parentName + "'");
			}
			// register a new child and establish the parent-child relationship
			TaxonBuilder child = this.registration.registerTaxon(this.rank.next(), name);
			this.children.add(child);
			child.parent = this;
			return child;
		}

		@Override
		public <A extends IAllele> void setDefaultChromosome(IChromosome<A> chromosome, A value, boolean required) {
			this.alleles.put(chromosome, new ITaxon.TaxonAllele(value, required));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			TaxonBuilder that = (TaxonBuilder) o;

			if (this.rank != that.rank) {
				return false;
			}
			return this.name.equals(that.name);
		}

		@Override
		public int hashCode() {
			int result = this.rank.hashCode();
			result = 31 * result + this.name.hashCode();
			return result;
		}
	}
}
