package forestry.api.genetics.alleles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpecies;
import forestry.api.plugin.IGenomeBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

/**
 * A karyotype is the set of all chromosomes that make up a species type's genome.
 * It also defines what alleles are possible values for each chromosome in the genome.
 * The karyotype also creates a {@link Codec} for genomes of members of this species.
 */
public interface IKaryotype {
	/**
	 * @return All chromosomes types of this IKaryotype, in the order they were defined.
	 */
	ImmutableList<IChromosome<?>> getChromosomes();

	/**
	 * Checks if this karyotype contains the given type.
	 */
	boolean contains(IChromosome<?> chromosome);

	/**
	 * @return The chromosome that determines this individual's species.
	 */
	IRegistryChromosome<? extends ISpecies<?>> getSpeciesChromosome();

	/**
	 * @return The number of chromosomes in this karyotype.
	 */
	int size();

	/**
	 * @return Whether given chromosome is part of this karyotype.
	 */
	<A extends IAllele> boolean isChromosomeValid(IChromosome<A> chromosome);

	/**
	 * Returns this karyotype's default allele for the chromosome.
	 * Use {@link ISpecies#getDefaultGenome} and {@link IGenome#getActiveAllele} if you know the species.
	 *
	 * @return The default allele for the given chromosome in this karyotype.
	 */
	<A extends IAllele> A getDefaultAllele(IChromosome<A> chromosome);

	/**
	 * A weakly inherited chromosome is a chromosome whose default allele is always overridden by non-default alleles
	 * during inheritance. For example, a bee's temperature tolerance
	 *
	 * @return Whether this chromosome is "weakly inherited" or "secondary."
	 */
	boolean isWeaklyInherited(IChromosome<?> chromosome);

	/**
	 * @return The default species for this species type.
	 */
	ResourceLocation getDefaultSpecies();

	/**
	 * @return {@code true} if the given allele can be set for this chromosome.
	 */
	<A extends IAllele, B extends A> boolean isAlleleValid(IChromosome<A> chromosome, B allele);

	Codec<IGenome> getGenomeCodec();

	ImmutableMap<IChromosome<?>, ? extends IAllele> getDefaultAlleles();

	/**
	 * @return A new genome builder using this karyotype.
	 */
	IGenomeBuilder createGenomeBuilder();

	/**
	 * Gets the list of valid alleles for the given chromosome.
	 *
	 * @param chromosome The chromosome to retrieve valid alleles for.
	 * @param <A>        The type of allele contained by the chromosome.
	 * @return An immutable list of valid alleles permitted for the chromosome in this karyotype.
	 * @throws IllegalArgumentException If the chromosome is not present in this karyotype.
	 */
	<A extends IAllele> Collection<A> getAlleles(IChromosome<A> chromosome);
}
