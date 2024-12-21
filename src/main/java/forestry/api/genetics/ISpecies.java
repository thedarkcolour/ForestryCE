package forestry.api.genetics;

import java.util.List;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.alleles.IRegistryAlleleValue;

public interface ISpecies<I extends IIndividual> extends IRegistryAlleleValue {
	/**
	 * @return The translation key for the human-readable name of this species.
	 */
	String getTranslationKey();

	/**
	 * @return The human-readable name of this species.
	 */
	default MutableComponent getDisplayName() {
		return Component.translatable(getTranslationKey());
	}

	/**
	 * @return The translation key for the description of this species used by the Portable Analyzer.
	 */
	default String getDescriptionTranslationKey() {
		return getTranslationKey() + ".desc";
	}

	/**
	 * @return The default genome for this species.
	 */
	IGenome getDefaultGenome();

	/**
	 * @return The unique identifier of this species. Also the allele ID for this species.
	 */
	ResourceLocation id();

	/**
	 * @return The scientific name of this species, including the genus and species.
	 */
	String getBinomial();

	/**
	 * @return The scientific species name, the second half of the scientific name after the genus.
	 */
	String getSpeciesName();

	/**
	 * @return The genus of this species.
	 */
	ITaxon getGenus();

	default String getGenusName() {
		return getGenus().name();
	}

	/**
	 * @return The type of species this is.
	 */
	ISpeciesType<? extends ISpecies<I>, I> getType();

	/**
	 * @return Whether this species is "secret" (Ex. Easter egg bee or extremely rare tree/butterfly)
	 */
	boolean isSecret();

	int getComplexity();

	default ItemStack createStack(I individual, ILifeStage stage) {
		return getType().createStack(individual, stage);
	}

	default ItemStack createStack(ILifeStage stage) {
		return createStack(createIndividual(), stage);
	}

	/**
	 * Creates an individual of this species using the default genome and the added alleles.
	 *
	 * @param alleles A map of alleles to set on this individual upon creation. Any missing chromosomes use default alleles.
	 * @return An individual along with any specified alleles.
	 */
	I createIndividual(Map<IChromosome<?>, IAllele> alleles);

	I createIndividualFromPairs(Map<IChromosome<?>, AllelePair<?>> allelePairs);

	/**
	 * Creates an individual of this species using the specified genome.
	 *
	 * @param genome The genome for this individual.
	 * @return The new individual.
	 * @throws IllegalArgumentException If the genome's karyotype does not match this species.
	 */
	I createIndividual(IGenome genome);

	/**
	 * @return A new individual of this species using the default genome.
	 */
	default I createIndividual() {
		return createIndividual(getDefaultGenome());
	}

	default IKaryotype getKaryotype() {
		return getDefaultGenome().getKaryotype();
	}

	/**
	 * @return Whether the item form of this species has an enchantment glint.
	 */
	boolean hasGlint();

	/**
	 * @return Whether the allele for this species is dominant or recessive.
	 */
	@Override
	boolean isDominant();

	String getAuthority();

	/**
	 * @return The color of cells that contain this species in the Escritoire research game
	 */
	int getEscritoireColor();

	default <S extends ISpecies<?>> S cast() {
		return (S) this;
	}

	void addTooltip(I individual, List<Component> tooltip);
}
