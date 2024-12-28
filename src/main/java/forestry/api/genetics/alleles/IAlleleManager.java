package forestry.api.genetics.alleles;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;

/**
 * Used to retrieve interned instances of alleles. Also exposes methods for creating chromosomes.
 * Store alleles obtained from these method calls in a static class like Forestry does in {@link ForestryAlleles}.
 * This class is not thread-safe.
 *
 * @see ForestryAlleles For preexisting alleles. Use those whenever possible, copying them to other fields is fine.
 */
public interface IAlleleManager {
	/**
	 * @return A recessive integer allele with the given value.
	 */
	default IIntegerAllele intAllele(int value) {
		return intAllele(value, false);
	}

	/**
	 * Creates a new allele for the given value, or retrieves a pre-existing allele if one has already been made.
	 *
	 * @param value    The value of this allele.
	 * @param dominant If this allele is dominant.
	 * @return A new allele, or an existing one if this allele has already been created elsewhere.
	 */
	IIntegerAllele intAllele(int value, boolean dominant);

	default IFloatAllele floatAllele(float value) {
		return floatAllele(value, false);
	}

	IFloatAllele floatAllele(float value, boolean dominant);

	default <V> IValueAllele<V> valueAllele(V value, IAlleleNaming<V> naming) {
		return valueAllele(value, false, naming);
	}

	<V> IValueAllele<V> valueAllele(V value, boolean dominant, IAlleleNaming<V> naming);

	/**
	 * @return A new boolean allele.
	 */
	IBooleanAllele booleanAllele(boolean value, boolean dominant);

	<V extends IRegistryAlleleValue> IRegistryAllele<V> registryAllele(ResourceLocation id, IRegistryChromosome<V> chromosome);

	/**
	 * @return The allele codec, which serializes/deserializes using the allele's ID.
	 */
	Codec<IAllele> alleleCodec();

	/**
	 * @return The chromosome codec, which serializes/deserializes using the chromosome's ID.
	 */
	Codec<IChromosome<?>> chromosomeCodec();

	/**
	 * @return An allele with the given ID, or {@code null} if no allele has been created with that ID.
	 */
	@Nullable
	IAllele getAllele(ResourceLocation id);

	IFloatChromosome floatChromosome(ResourceLocation id);

	IIntegerChromosome intChromosome(ResourceLocation id);

	IBooleanChromosome booleanChromosome(ResourceLocation id);

	<V> IValueChromosome<V> valueChromosome(ResourceLocation id, Class<V> valueClass);

	/**
	 * Creates and registers a registry chromosome, which is responsible for populating
	 * the values of its corresponding {@link IRegistryAllele} instances.
	 *
	 * @param id         The ID of this chromosome.
	 * @param valueClass The type of value this chromosome's alleles hold. Must implement {@link IRegistryAlleleValue}.
	 * @param <V>        The type of value contained by the alleles of this chromosome.
	 * @return A new registry chromosome.
	 */
	<V extends IRegistryAlleleValue> IRegistryChromosome<V> registryChromosome(ResourceLocation id, Class<V> valueClass);

	/**
	 * @return A chromosome with the given ID, or {@code null} if no chromosome has been created with that ID.
	 * @since 2.1.1
	 */
	@Nullable
	IChromosome<?> getChromosome(ResourceLocation id);
}
