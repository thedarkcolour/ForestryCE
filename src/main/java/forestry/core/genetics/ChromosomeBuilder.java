package forestry.core.genetics;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.List;

import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.plugin.IChromosomeBuilder;

public class ChromosomeBuilder<A extends IAllele> implements IChromosomeBuilder<A> {
	final IChromosome<A> chromosome;
	final ImmutableSet.Builder<A> alleles;
	@Nullable
	A defaultAllele;
	boolean weaklyInherited;

	public ChromosomeBuilder(IChromosome<A> chromosome) {
		this.chromosome = chromosome;
		this.alleles = ImmutableSet.builder();
	}

	@Override
	public IChromosomeBuilder<A> addAlleles(List<A> alleles) {
		this.alleles.addAll(alleles);
		return this;
	}

	@Override
	public IChromosomeBuilder<A> setDefault(A allele) {
		this.defaultAllele = allele;
		return this;
	}

	@Override
	public IChromosomeBuilder<A> setWeaklyInherited(boolean weaklyInherited) {
		this.weaklyInherited = weaklyInherited;
		return this;
	}
}
