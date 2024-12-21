package forestry.core.genetics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.IdentityHashMap;
import java.util.Map;

import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.plugin.IGenomeBuilder;

public final class Genome implements IGenome {
	final ImmutableMap<IChromosome<?>, AllelePair<?>> chromosomes;
	private final IKaryotype karyotype;

	private boolean isDefaultGenome;
	private boolean hasCachedDefaultGenome;

	public Genome(IKaryotype karyotype, ImmutableMap<IChromosome<?>, AllelePair<?>> chromosomes) {
		this.karyotype = karyotype;
		this.chromosomes = chromosomes;
	}

	// Used by codec only
	public static IGenome fromUnsortedAlleles(Karyotype karyotype, Map<IChromosome<?>, AllelePair<?>> map) {
		ImmutableMap.Builder<IChromosome<?>, AllelePair<?>> sorted = ImmutableMap.builderWithExpectedSize(map.size());
		for (IChromosome<?> chromosome : karyotype.getChromosomes()) {
			sorted.put(chromosome, map.get(chromosome));
		}
		return new Genome(karyotype, sorted.buildOrThrow());
	}

	@Override
	public ImmutableList<AllelePair<?>> getAllelePairs() {
		return this.chromosomes.values().asList();
	}

	@Override
	public IKaryotype getKaryotype() {
		return this.karyotype;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends IAllele> AllelePair<A> getAllelePair(IChromosome<A> chromosomeType) {
		return (AllelePair<A>) this.chromosomes.get(chromosomeType);
	}

	@Override
	public boolean isDefaultGenome() {
		if (!this.hasCachedDefaultGenome) {
			Genome defaultGenome = (Genome) getActiveValue(this.karyotype.getSpeciesChromosome()).getDefaultGenome();

			this.isDefaultGenome = this == defaultGenome || (isSameAlleles(defaultGenome));
			this.hasCachedDefaultGenome = true;
		}

		return this.isDefaultGenome;
	}

	@Override
	public ImmutableMap<IChromosome<?>, AllelePair<?>> getChromosomes() {
		return this.chromosomes;
	}

	@Override
	public IGenome copyWithPairs(Map<IChromosome<?>, AllelePair<?>> alleles) {
		if (alleles.isEmpty()) {
			return this;
		} else {
			Genome.Builder builder = new Genome.Builder(getKaryotype());
			// avoid duplicate instances of default genome
			boolean isDefault = true;

			// copy over allele map or default allele
			for (Map.Entry<IChromosome<?>, AllelePair<?>> entry : this.chromosomes.entrySet()) {
				IChromosome<?> chromosome = entry.getKey();
				AllelePair<?> pair = entry.getValue();
				AllelePair<?> allele = alleles.get(chromosome);

				if (allele == null || allele.equals(pair)) {
					// copy default allele if missing or equal
					builder.setUnchecked(chromosome, pair);
				} else {
					// mark not default when there are non-default alleles
					builder.setUnchecked(chromosome, allele);
					isDefault = false;
				}
			}

			if (isDefault) {
				return this;
			} else {
				return builder.build();
			}
		}
	}

	@Override
	public boolean isSameAlleles(IGenome other) {
		return other.getKaryotype() == this.karyotype && this.chromosomes.equals(other.getChromosomes());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Genome genome = (Genome) o;

		if (!chromosomes.equals(genome.chromosomes)) {
			return false;
		}
		return karyotype.equals(genome.karyotype);
	}

	@Override
	public int hashCode() {
		int result = chromosomes.hashCode();
		result = 31 * result + karyotype.hashCode();
		return result;
	}

	public static class Builder implements IGenomeBuilder {
		private final IKaryotype karyotype;
		private final IdentityHashMap<IChromosome<?>, IAllele> active = new IdentityHashMap<>();
		private final IdentityHashMap<IChromosome<?>, IAllele> inactive = new IdentityHashMap<>();

		public Builder(IKaryotype karyotype) {
			Preconditions.checkNotNull(karyotype);

			this.karyotype = karyotype;
		}

		@Override
		public <A extends IAllele> void set(IChromosome<A> chromosome, A allele) {
			if (this.karyotype.isAlleleValid(chromosome, allele)) {
				this.active.put(chromosome, allele);
				this.inactive.put(chromosome, allele);
			}
		}

		@Override
		public <A extends IAllele> void setActive(IChromosome<A> chromosome, A allele) {
			if (this.karyotype.isAlleleValid(chromosome, allele)) {
				this.active.put(chromosome, allele);
			} else {
				throw new IllegalArgumentException("Invalid allele " + allele.alleleId() + " for chromosome " + chromosome.id());
			}
		}

		@Override
		public <A extends IAllele> void setInactive(IChromosome<A> chromosome, A allele) {
			if (this.karyotype.isAlleleValid(chromosome, allele)) {
				this.inactive.put(chromosome, allele);
			}
		}

		@Override
		public boolean isEmpty() {
			return this.inactive.isEmpty() && this.active.isEmpty();
		}

		@Override
		public void setRemainingDefault() {
			for (Map.Entry<IChromosome<?>, ? extends IAllele> entry : this.karyotype.getDefaultAlleles().entrySet()) {
				IChromosome<?> chromosome = entry.getKey();
				IAllele defaultAllele = entry.getValue();

				if (!this.active.containsKey(chromosome)) {
					this.active.put(chromosome, defaultAllele);
				}
				if (!this.inactive.containsKey(chromosome)) {
					this.inactive.put(chromosome, defaultAllele);
				}
			}
		}

		@Override
		public IGenome build() {
			// Check for missing chromosomes and display which ones are missing
			if (this.karyotype.size() != this.active.size()) {
				StringBuilder msg = new StringBuilder("Tried to build genome, but the following chromosomes are missing from the karyotype: { ");

				for (IChromosome<?> chromosome : this.karyotype.getChromosomes()) {
					if (!this.active.containsKey(chromosome)) {
						msg.append(chromosome.id());
						msg.append(' ');
					}
				}
				msg.append('}');

				throw new IllegalStateException(msg.toString());
			} else {
				ImmutableMap.Builder<IChromosome<?>, AllelePair<?>> genome = new ImmutableMap.Builder<>();

				for (IChromosome<?> chromosome : this.karyotype.getChromosomes()) {
					IAllele active = this.active.get(chromosome);
					IAllele inactive = this.inactive.get(chromosome);

					// Check for incomplete pairs
					if (active == null || inactive == null) {
						throw new IllegalStateException("Tried to build genome, but the allele pair was incomplete for the following chromosome: " + chromosome.id());
					} else {
						genome.put(chromosome, new AllelePair<>(active, inactive));
					}
				}

				return new Genome(this.karyotype, genome.build());
			}
		}
	}
}
