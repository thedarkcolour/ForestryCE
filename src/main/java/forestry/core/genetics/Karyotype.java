package forestry.core.genetics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;

import forestry.api.IForestryApi;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.alleles.IRegistryChromosome;
import forestry.api.plugin.IChromosomeBuilder;
import forestry.api.plugin.IGenomeBuilder;
import forestry.api.plugin.IKaryotypeBuilder;

public class Karyotype implements IKaryotype {
	private final ImmutableMap<IChromosome<?>, ImmutableSet<? extends IAllele>> chromosomes;
	private final IRegistryChromosome<? extends ISpecies<?>> speciesChromosome;
	private final ImmutableMap<IChromosome<?>, ? extends IAllele> defaultAlleles;
	private final ResourceLocation defaultSpecies;
	private final Set<IChromosome<?>> weaklyInheritedChromosomes;
	private final Codec<IGenome> genomeCodec;

	// Used in Karyotype.Builder
	public Karyotype(ImmutableMap<IChromosome<?>, ImmutableSet<? extends IAllele>> chromosomes, ImmutableMap<IChromosome<?>, ? extends IAllele> defaultAlleles, ResourceLocation defaultSpecies, Set<IChromosome<?>> weaklyInheritedChromosomes) {
		this.chromosomes = chromosomes;
		this.speciesChromosome = (IRegistryChromosome<? extends ISpecies<?>>) chromosomes.keySet().asList().get(0);
		this.defaultAlleles = defaultAlleles;
		this.defaultSpecies = defaultSpecies;
		this.weaklyInheritedChromosomes = weaklyInheritedChromosomes;

		Keyable chromosomesKeyable = Keyable.forStrings(() -> this.chromosomes.keySet().stream().map(chromosome -> chromosome.id().toString()));
		this.genomeCodec = Codec.simpleMap(IForestryApi.INSTANCE.getAlleleManager().chromosomeCodec(), AllelePair.CODEC, chromosomesKeyable)
				.xmap(map -> Genome.sanitizeAlleles(this, map), IGenome::getChromosomes).codec();
	}

	@Override
	public ImmutableList<IChromosome<?>> getChromosomes() {
		// asList caches the returned list, no allocations to worry about
		return this.chromosomes.keySet().asList();
	}

	@Override
	public boolean contains(IChromosome<?> chromosome) {
		return this.chromosomes.containsKey(chromosome);
	}

	@Override
	public IRegistryChromosome<? extends ISpecies<?>> getSpeciesChromosome() {
		return this.speciesChromosome;
	}

	@Override
	public int size() {
		return this.chromosomes.size();
	}

	@Override
	public <A extends IAllele> boolean isAlleleValid(IChromosome<A> chromosome, A allele) {
		ImmutableSet<? extends IAllele> validAlleles = this.chromosomes.get(chromosome);

		if (validAlleles != null) {
			if (chromosome instanceof IRegistryChromosome<?> registry) {
				return !registry.isPopulated() || registry.isValidAllele(allele);
			} else {
				return validAlleles.contains(allele);
			}
		}

		return false;
	}

	@Override
	public <A extends IAllele> boolean isChromosomeValid(IChromosome<A> chromosome) {
		return this.chromosomes.containsKey(chromosome);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends IAllele> A getDefaultAllele(IChromosome<A> chromosome) {
		A allele = (A) this.defaultAlleles.get(chromosome);
		if (allele == null) {
			throw new IllegalArgumentException("Chromosome is not valid");
		}
		return allele;
	}

	@Override
	public boolean isWeaklyInherited(IChromosome<?> chromosome) {
		return this.weaklyInheritedChromosomes.contains(chromosome);
	}

	@SuppressWarnings({"DataFlowIssue", "unchecked"})
	@Override
	public <A extends IAllele> Collection<A> getAlleles(IChromosome<A> chromosome) {
		Preconditions.checkArgument(isChromosomeValid(chromosome), "Chromosome not present in karyotype");

		ImmutableSet<? extends IAllele> validAlleles = this.chromosomes.get(chromosome);
		if (validAlleles.isEmpty()) {
			return (Collection<A>) ((IRegistryChromosome<?>) chromosome).alleles();
		} else {
			return (Collection<A>) validAlleles.asList();
		}
	}

	@Override
	public ImmutableMap<IChromosome<?>, ? extends IAllele> getDefaultAlleles() {
		return this.defaultAlleles;
	}

	@Override
	public IGenomeBuilder createGenomeBuilder() {
		return new Genome.Builder(this);
	}

	@Override
	public ResourceLocation getDefaultSpecies() {
		return this.defaultSpecies;
	}

	@Override
	public Codec<IGenome> getGenomeCodec() {
		return this.genomeCodec;
	}

	public static class Builder implements IKaryotypeBuilder {
		private final LinkedHashMap<IChromosome<?>, ChromosomeBuilder<?>> chromosomes = new LinkedHashMap<>();
		@Nullable
		private IRegistryChromosome<? extends ISpecies<?>> speciesChromosome;
		@Nullable
		private ResourceLocation defaultSpeciesId;

		@Override
		public void setSpecies(IRegistryChromosome<? extends ISpecies<?>> species, ResourceLocation defaultId) {
			if (this.speciesChromosome != null && this.speciesChromosome != species) {
				throw new IllegalStateException("The species chromosome for this karyotype has already been set: " + this.speciesChromosome.id() + ", but tried setting to " + species.id());
			} else {
				this.speciesChromosome = species;
				this.defaultSpeciesId = defaultId;
			}
		}

		@Override
		public void set(IRegistryChromosome<?> chromosome, ResourceLocation defaultId) {
			this.chromosomes.computeIfAbsent(chromosome, key -> new ChromosomeBuilder<>(chromosome));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <A extends IAllele> IChromosomeBuilder<A> get(IChromosome<A> chromosome) {
			return (IChromosomeBuilder<A>) this.chromosomes.computeIfAbsent(chromosome, key -> new ChromosomeBuilder<>(chromosome));
		}

		@SuppressWarnings("UnstableApiUsage")
		public Karyotype build() {
			Preconditions.checkState(this.defaultSpeciesId != null && this.speciesChromosome != null, "IKaryotypeBuilder is missing a species chromosome.");

			ImmutableMap.Builder<IChromosome<?>, ImmutableSet<? extends IAllele>> permittedAlleles = ImmutableMap.builderWithExpectedSize(this.chromosomes.size() + 1);
			ImmutableMap.Builder<IChromosome<?>, IAllele> defaultAlleles = ImmutableMap.builderWithExpectedSize(this.chromosomes.size() + 1);
			Set<IChromosome<?>> weaklyInheritedChromosomes = Collections.newSetFromMap(new IdentityHashMap<>());

			// Species chromosome goes first
			permittedAlleles.put(this.speciesChromosome, ImmutableSet.of());
			defaultAlleles.put(this.speciesChromosome, IForestryApi.INSTANCE.getAlleleManager().registryAllele(this.defaultSpeciesId, this.speciesChromosome));

			for (Map.Entry<IChromosome<?>, ChromosomeBuilder<?>> entry : this.chromosomes.entrySet()) {
				IChromosome<?> chromosome = entry.getKey();
				ChromosomeBuilder<?> builder = entry.getValue();
				ImmutableSet<? extends IAllele> permitted = builder.alleles.build();
				// registry alleles are added later
				if (!(chromosome instanceof IRegistryChromosome<?>) && permitted.isEmpty()) {
					throw new IllegalStateException("Chromosome missing permitted alleles in karyotype.");
				}
				if (builder.defaultAllele == null) {
					throw new IllegalStateException("Chromosome \"" + chromosome.id() + "\" has no default allele. Please set one in the karyotype for the species " + this.speciesChromosome.id());
				}
				permittedAlleles.put(chromosome, permitted);
				defaultAlleles.put(chromosome, builder.defaultAllele);

				if (builder.weaklyInherited) {
					weaklyInheritedChromosomes.add(chromosome);
				}
			}

			return new Karyotype(permittedAlleles.build(), defaultAlleles.build(), this.defaultSpeciesId, weaklyInheritedChromosomes);
		}
	}
}
