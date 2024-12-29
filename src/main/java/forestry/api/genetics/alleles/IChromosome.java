package forestry.api.genetics.alleles;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * In Forestry, a chromosome is a key in the genome that maps to different alleles.
 */
public interface IChromosome<A extends IAllele> {
	/**
	 * @return Unique ID for this chromosome.
	 */
	ResourceLocation id();

	/**
	 * @return The translation key of this allele, for use in {@link Component#translatable}.
	 */
	String getTranslationKey(A allele);

	/**
	 * @return The display name for the allele when representing this chromosome. (ex. Shortest, Largest, Yes, No)
	 */
	default MutableComponent getDisplayName(A allele) {
		return Component.translatable(getTranslationKey(allele));
	}

	/**
	 * @return The translation key for the name of this chromosome.
	 * @since 2.1.1
	 */
	String getChromosomeTranslationKey();

	/**
	 * @return The human-readable name of this chromosome. (ex. Lifespan, Fertility, Girth)
	 * @since 2.1.1
	 */
	default MutableComponent getChromosomeDisplayName() {
		return Component.translatable(getChromosomeTranslationKey());
	}

	Class<?> valueClass();
}
