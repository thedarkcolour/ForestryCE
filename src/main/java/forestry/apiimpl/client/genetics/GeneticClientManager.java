package forestry.apiimpl.client.genetics;

import java.util.IdentityHashMap;

import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.client.genetics.IGeneticClientManager;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;

import org.jetbrains.annotations.Nullable;

public class GeneticClientManager implements IGeneticClientManager {
	private final IdentityHashMap<ISpeciesType<?, ?>, IAnalyzerPlugin<?, ?>> plugins;

	public GeneticClientManager(IdentityHashMap<ISpeciesType<?, ?>, IAnalyzerPlugin<?, ?>> plugins) {
		this.plugins = plugins;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <S extends ISpecies<I>, I extends IIndividual> IAnalyzerPlugin<S, I> getAnalyzerPlugin(ISpeciesType<S, I> type) {
		return (IAnalyzerPlugin<S, I>) this.plugins.get(type);
	}
}
