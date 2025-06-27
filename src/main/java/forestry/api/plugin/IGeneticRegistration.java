package forestry.api.plugin;

import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.filter.IFilterRuleType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Handles registration of genetic-related data. Accessed from {@link IForestryPlugin#registerGenetics(IGeneticRegistration)}.
 */
public interface IGeneticRegistration {
	/**
	 * Defines a new taxon under an existing parent. Taxa are displayed as classifications in the Forestry analyzer.
	 * When defining a new tree of taxa, it is better to use {@link #defineTaxon(String, String, Consumer)}
	 * instead of making repeated calls to this method.
	 *
	 * @param parent The name of the parent taxon. Cannot be a {@link forestry.api.genetics.TaxonomicRank#GENUS}.
	 * @param name   The name of the taxon. Must be unique.
	 * @throws UnsupportedOperationException If the parent taxon is a GENUS.
	 * @see forestry.api.genetics.ForestryTaxa For built-in taxon names.
	 */
	void defineTaxon(String parent, String name);

	/**
	 * Defines a new taxon or retrieves an existing taxon, allows for adding subtaxa and species.
	 *
	 * @param parent The name of the parent taxon. Cannot be a {@link forestry.api.genetics.TaxonomicRank#GENUS}.
	 * @param name   The name of the taxon. Must be unique for all taxa in the same rank.
	 * @param action A consumer that adds additional information to the taxon after it is defined.
	 * @throws UnsupportedOperationException If the parent taxon is a GENUS.
	 * @see forestry.api.genetics.ForestryTaxa For builtin taxon names.
	 */
	void defineTaxon(String parent, String name, Consumer<ITaxonBuilder> action);

	/**
	 * Modify an existing species, for example, adding an extra chromosome to bees, or adding additional permitted alleles to chromosomes.
	 * Called after all species types are registered, but before the registry is finalized.
	 *
	 * @param id     The ID of the species to modify.
	 * @param action The modifications to be made.
	 */
	void modifySpeciesType(ResourceLocation id, Consumer<ISpeciesTypeBuilder> action);

	/**
	 * Registers a new filter rule for the Genetic Filter block.
	 *
	 * @param ruleType The filter rule type to register.
	 */
	void registerFilterRuleType(IFilterRuleType ruleType);

	default void registerFilterRuleTypes(IFilterRuleType[] ruleTypes) {
		for (IFilterRuleType ruleType : ruleTypes) {
			registerFilterRuleType(ruleType);
		}
	}
}
