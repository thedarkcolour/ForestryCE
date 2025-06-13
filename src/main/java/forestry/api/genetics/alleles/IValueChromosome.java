package forestry.api.genetics.alleles;

/**
 * A chromosome that holds non-primitive values.
 * If the type of value held by a value chromosome needs to be registered after species types are registered,
 * then a {@link IRegistryChromosome} should be used instead as its values can be registered later.
 * Create an instance using {@link IAlleleManager#valueChromosome}.
 *
 * @param <V> The type of value held by the alleles of this chromosome.
 */
public interface IValueChromosome<V> extends IChromosome<IValueAllele<? extends V>> {
}
