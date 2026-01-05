package forestry.api.client.genetics;

import javax.annotation.Nullable;

import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;

public interface IGeneticClientManager {
	/**
	 * Retrieves the new-style analyzer plugin used to display genetic information.
	 *
	 * @param type The species type to display information for
	 * @return The plugin to use for displaying information, or {@code null} if no plugin was registered.
	 */
	@Nullable
	<S extends ISpecies<I>, I extends IIndividual> IAnalyzerPlugin<S, I> getAnalyzerPlugin(ISpeciesType<S, I> type);
}
