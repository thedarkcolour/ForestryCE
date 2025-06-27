package forestry.api.plugin;

import forestry.api.genetics.alleles.IAllele;

import java.util.List;

/**
 * Interface for customizing a default chromosome in a karyotype.
 *
 * @param <A> The type of alleles that represent this chromosome.
 */
public interface IChromosomeBuilder<A extends IAllele> {
	/**
	 * Add alleles as valid values for this chromosome.
	 */
	IChromosomeBuilder<A> addAlleles(List<? extends A> alleles);

	/**
	 * Override the default value of this chromosome that was previously set in {@link IKaryotypeBuilder#set}.
	 */
	IChromosomeBuilder<A> setDefault(A allele);

	/**
	 * Sets whether this chromosome "weakly inherited."
	 * <p>
	 * If a chromosome is weakly inherited, then its default allele will always be overridden by
	 * a non-default allele during inheritance or mutations. An example in Forestry is the temperature and humidity
	 * tolerance chromosomes, which are weakly inherited. This ensures that breeding a Common bee using a Modest
	 * princess and Savanna drone will produce a Common bee that can survive either in the Savanna or in the Desert,
	 * instead of a Common bee that can only survive in the plains biome like in older versions of Forestry.
	 *
	 * @param weaklyInherited Whether this chromosome should be weakly inherited.
	 */
	IChromosomeBuilder<A> setWeaklyInherited(boolean weaklyInherited);
}
