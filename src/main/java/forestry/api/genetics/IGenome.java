package forestry.api.genetics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.network.chat.MutableComponent;

import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IBooleanChromosome;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IFloatChromosome;
import forestry.api.genetics.alleles.IIntegerChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.alleles.IValueChromosome;

/**
 * Holds the {@link AllelePair}s which comprise the traits of a given individual.
 */
public interface IGenome {
	/**
	 * @return A array with all chromosomes of this genome.
	 */
	ImmutableList<AllelePair<?>> getAllelePairs();

	/**
	 * Returns the allele pair containing the active and inactive alleles of the given chromosome.
	 * Uses an unchecked cast.
	 *
	 * @return The allele pair for the given chromosome.
	 */
	<A extends IAllele> AllelePair<A> getAllelePair(IChromosome<A> chromosomeType);

	/**
	 * @return {@code true} if this genome is equal to the default genome of the species, as specified in the Karyotype.
	 */
	boolean isDefaultGenome();

	/**
	 * @return The karyotype of this genome. It defines the positions of the chromosomes in the array and the length
	 * of it.
	 */
	IKaryotype getKaryotype();

	ImmutableMap<IChromosome<?>, AllelePair<?>> getChromosomes();

	/**
	 * Copies this genome, setting active and inactive alleles using the given map.
	 * If the resulting genome is the same as this genome, then no copy is made.
	 *
	 * @param alleles Map of alleles that should be in the copy.
	 * @return A copy of this genome with alleles from the given map. If result is equal to this, then just returns this.
	 */
	default IGenome copyWith(Map<IChromosome<?>, IAllele> alleles) {
		IdentityHashMap<IChromosome<?>, AllelePair<?>> pairMap = new IdentityHashMap<>(alleles.size());

		for (Map.Entry<IChromosome<?>, IAllele> entry : alleles.entrySet()) {
			IChromosome<?> chromosome = entry.getKey();
			IAllele allele = entry.getValue();
			pairMap.put(chromosome, new AllelePair<>(allele, allele));
		}

		return copyWithPairs(pairMap);
	}

	/**
	 * Copies this genome, setting active and inactive alleles using the given map.
	 * If the resulting genome is the same as this genome, then no copy is made.
	 *
	 * @param allelePairs Map of allele pairs that should be in the copy.
	 * @return The new genome.
	 */
	IGenome copyWithPairs(Map<IChromosome<?>, AllelePair<?>> allelePairs);

	/**
	 * @return {@code true} if this genome has the same karyotype and alleles as the other genome.
	 */
	boolean isSameAlleles(IGenome other);

	/**
	 * @return The active allele of the chromosome with the given type.
	 */
	default <A extends IAllele> A getActiveAllele(IChromosome<A> chromosome) {
		return getAllelePair(chromosome).active();
	}

	default float getActiveValue(IFloatChromosome chromosome) {
		return getActiveAllele(chromosome).value();
	}

	default boolean getActiveValue(IBooleanChromosome chromosome) {
		return getActiveAllele(chromosome).value();
	}

	default int getActiveValue(IIntegerChromosome chromosome) {
		return getActiveAllele(chromosome).value();
	}

	default <V> V getActiveValue(IValueChromosome<V> chromosome) {
		return getActiveAllele(chromosome).value();
	}

	/**
	 * @return The inactive allele of the chromosome with the given type.
	 */
	default <A extends IAllele> A getInactiveAllele(IChromosome<A> chromosome) {
		return getAllelePair(chromosome).inactive();
	}

	default float getInactiveValue(IFloatChromosome chromosome) {
		return getInactiveAllele(chromosome).value();
	}

	default boolean getInactiveValue(IBooleanChromosome chromosome) {
		return getInactiveAllele(chromosome).value();
	}

	default int getInactiveValue(IIntegerChromosome chromosome) {
		return getInactiveAllele(chromosome).value();
	}

	default <V> V getInactiveValue(IValueChromosome<V> chromosome) {
		return getInactiveAllele(chromosome).value();
	}

	default MutableComponent getActiveName(IFloatChromosome chromosome) {
		return chromosome.getDisplayName(getActiveAllele(chromosome));
	}

	default MutableComponent getActiveName(IBooleanChromosome chromosome) {
		return chromosome.getDisplayName(getActiveAllele(chromosome));
	}

	default MutableComponent getActiveName(IIntegerChromosome chromosome) {
		return chromosome.getDisplayName(getActiveAllele(chromosome));
	}

	default <V> MutableComponent getActiveName(IValueChromosome<V> chromosome) {
		return chromosome.getDisplayName(getActiveAllele(chromosome));
	}

	default MutableComponent getInactiveName(IFloatChromosome chromosome) {
		return chromosome.getDisplayName(getInactiveAllele(chromosome));
	}

	default MutableComponent getInactiveName(IBooleanChromosome chromosome) {
		return chromosome.getDisplayName(getInactiveAllele(chromosome));
	}

	default MutableComponent getInactiveName(IIntegerChromosome chromosome) {
		return chromosome.getDisplayName(getInactiveAllele(chromosome));
	}

	default <V> MutableComponent getInactiveName(IValueChromosome<V> chromosome) {
		return chromosome.getDisplayName(getInactiveAllele(chromosome));
	}

	/**
	 * Note: Use {@link IIndividual#getSpecies} whenever possible.
	 *
	 * @return The active species of the individual.
	 */
	default <S extends ISpecies<?>> S getActiveSpecies() {
		return (S) getActiveValue(getKaryotype().getSpeciesChromosome());
	}

	/**
	 * Note: Use {@link IIndividual#getInactiveSpecies} whenever possible.
	 *
	 * @return The inactive species of the individual.
	 */
	default <S extends ISpecies<?>> S getInactiveSpecies() {
		return (S) getInactiveValue(getKaryotype().getSpeciesChromosome());
	}
}
