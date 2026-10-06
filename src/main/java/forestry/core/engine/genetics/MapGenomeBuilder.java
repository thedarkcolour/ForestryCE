package forestry.core.engine.genetics;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.alleles.Allele;
import forestry.api.core.genetics.alleles.AlleleOverride;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.plugin.IGenomeBuilder;

// records each side separately so a one-sided genome closure generates a one-sided override
public class MapGenomeBuilder implements IGenomeBuilder {
	public final Map<ResourceLocation, AlleleOverride<?>> overrides = new LinkedHashMap<>();

	@Override
	public <V> void set(IChromosome<V> chromosome, Allele<V> allele) {
		this.overrides.put(chromosome.id(), AlleleOverride.both(allele));
	}

	@Override
	public void set(IChromosome<ResourceLocation> chromosome, ResourceLocation id) {
		this.overrides.put(chromosome.id(), AlleleOverride.both(Allele.reference(id)));
	}

	@Override
	public <V> void setActive(IChromosome<V> chromosome, Allele<V> allele) {
		record(chromosome, AlleleOverride.onlyActive(allele));
	}

	@Override
	public <V> void setInactive(IChromosome<V> chromosome, Allele<V> allele) {
		record(chromosome, AlleleOverride.onlyInactive(allele));
	}

	@Override
	public IGenome build() {
		return null;
	}

	@Override
	public void setRemainingDefault() {
		// datagen only needs overrides
	}

	@Override
	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}

	// The later call wins per side, matching Genome.Builder's two separate active/inactive maps
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void record(IChromosome<?> chromosome, AlleleOverride<?> override) {
		this.overrides.merge(chromosome.id(), override, (existing, added) -> ((AlleleOverride) existing).overrideWith(added));
	}
}
