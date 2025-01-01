package forestry.api.plugin;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.item.Item;

import forestry.api.genetics.ILifeStage;

import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import org.jetbrains.annotations.ApiStatus;

/**
 * Used to define the karyotype, life stages, and research materials for a new type of species.
 * The karyotype is the most important piece here, determining how genomes are structured.
 * This includes which chromosomes are allowed, which alleles those chromosomes can have,
 * the default alleles for those chromosomes, and most importantly, the species chromosome.
 */
public interface ISpeciesTypeBuilder {
	/**
	 * Defines the default karyotype for members of this species type. Although the default genome can be customized
	 * on a per-species basis, all members of the same species type have the same set of chromosomes.
	 * Multiple calls to this method will be chained and can override results of previous calls.
	 */
	ISpeciesTypeBuilder setKaryotype(Consumer<IKaryotypeBuilder> karyotype);

	/**
	 * Adds possible life stages to this species type.
	 * Make sure to also call {@link #setDefaultStage}.
	 */
	ISpeciesTypeBuilder addStages(ILifeStage... stages);

	/**
	 * Allows adding new Escritoire research materials or removing the default ones.
	 */
	ISpeciesTypeBuilder addResearchMaterials(Consumer<Reference2FloatMap<Item>> materials);

	/**
	 * Sets the default life stage of this species type.
	 *
	 * @param stage The default stage for use in menus.
	 */
	ISpeciesTypeBuilder setDefaultStage(ILifeStage stage);

	@ApiStatus.Internal
	ILifeStage getDefaultStage();

	@ApiStatus.Internal
	List<ILifeStage> getStages();

	@ApiStatus.Internal
	void buildResearchMaterials(Reference2FloatMap<Item> materialMap);
}
