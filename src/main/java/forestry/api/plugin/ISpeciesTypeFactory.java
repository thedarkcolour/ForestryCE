package forestry.api.plugin;

import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.IKaryotype;

public interface ISpeciesTypeFactory<T extends ISpeciesType<?, ?>> {
	T create(IKaryotype karyotype, ISpeciesTypeBuilder builder);
}
