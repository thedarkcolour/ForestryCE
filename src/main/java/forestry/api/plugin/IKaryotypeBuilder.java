package forestry.api.plugin;

import net.minecraft.resources.ResourceLocation;

import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IBooleanAllele;
import forestry.api.genetics.alleles.IBooleanChromosome;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IRegistryChromosome;

/**
 * Used to configure the the default set of chromosomes, called the karyotype, of a species.
 * Also configures the default genome.
 */
public interface IKaryotypeBuilder {
	/**
	 * Sets the species chromosome of the karyotype for this species type.
	 *
	 * @param species   The species chromosome.
	 * @param defaultId The ID of the default species, used as a fallback when a genome is not available or corrupt.
	 */
	void setSpecies(IRegistryChromosome<? extends ISpecies<?>> species, ResourceLocation defaultId);

	/**
	 * Sets the default allele of the chromosome in this karyotype and adds the chromosome if not already present.
	 *
	 * @param chromosome    The chromosome to add.
	 * @param defaultAllele The default value of the chromosome.
	 */
	default <A extends IAllele> IChromosomeBuilder<A> set(IChromosome<A> chromosome, A defaultAllele) {
		return get(chromosome).setDefault(defaultAllele);
	}

	/**
	 * Overload of {@link #set(IRegistryChromosome, ResourceLocation)} for booleans.
	 */
	default IChromosomeBuilder<IBooleanAllele> set(IBooleanChromosome chromosome, boolean defaultAllele) {
		return set(chromosome, defaultAllele ? ForestryAlleles.TRUE : ForestryAlleles.FALSE)
				.addAlleles(ForestryAlleles.DEFAULT_BOOLEANS);
	}

	/**
	 * Sets the default allele of the chromosome in this karyotype and adds the chromosome if not already present.
	 *
	 * @param chromosome The chromosome to add.
	 * @param defaultId  The ID of the default allele.
	 */
	void set(IRegistryChromosome<?> chromosome, ResourceLocation defaultId);

	/**
	 * Used to modify a chromosome already added in {@link #set}.
	 *
	 * @return The {@link IChromosomeBuilder} for this chromosome in the karyotype.
	 * @throws IllegalArgumentException If the chromosome is not contained in this genome.
	 */
	<A extends IAllele> IChromosomeBuilder<A> get(IChromosome<A> chromosome);
}
