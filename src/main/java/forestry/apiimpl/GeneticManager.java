package forestry.apiimpl;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.resources.ResourceLocation;

import forestry.api.genetics.IGeneticManager;
import forestry.api.genetics.IMutationManager;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.Taxon;

import org.jetbrains.annotations.ApiStatus;

public class GeneticManager implements IGeneticManager {
	private final ImmutableMap<String, Taxon> taxa;
	private final ImmutableMap<ResourceLocation, ISpeciesType<?, ?>> speciesTypes;
	@Nullable
	private ImmutableMap<ISpeciesType<?, ?>, IMutationManager<?>> mutationsByType;

	public GeneticManager(ImmutableMap<String, Taxon> taxa, ImmutableMap<ResourceLocation, ISpeciesType<?, ?>> speciesTypes) {
		this.taxa = taxa;
		this.speciesTypes = speciesTypes;
	}

	@Override
	public Taxon getTaxon(String name) {
		Taxon taxon = this.taxa.get(name);
		if (taxon == null) {
			throw new IllegalStateException("No taxon was registered with name '" + name + "'");
		}
		return taxon;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S extends ISpecies<?>> IMutationManager<S> getMutations(ISpeciesType<?, ?> speciesType) {
		if (this.mutationsByType == null) {
			throw new IllegalStateException("Mutations have not been registered yet");
		}
		IMutationManager<?> manager =  this.mutationsByType.get(speciesType);
		if (manager == null) {
			throw new IllegalStateException("Invalid or unregistered species type");
		}
		return (IMutationManager<S>) manager;
	}

	@Override
	public ISpeciesType<?, ?> getSpeciesType(ResourceLocation speciesTypeId) {
		ISpeciesType<?, ?> type = this.speciesTypes.get(speciesTypeId);
		if (type == null) {
			throw new IllegalStateException("No species type was registered with ID: " + speciesTypeId);
		}
		return type;
	}

	@Nullable
	@Override
	public ISpeciesType<?, ?> getSpeciesTypeSafe(ResourceLocation speciesTypeId) {
		return this.speciesTypes.get(speciesTypeId);
	}

	@Override
	public Collection<ISpeciesType<?, ?>> getSpeciesTypes() {
		return this.speciesTypes.values();
	}

	@ApiStatus.Internal
	public void setMutationsByType(ImmutableMap<ISpeciesType<?, ?>, IMutationManager<?>> mutationsByType) {
		this.mutationsByType = mutationsByType;
	}
}