package forestry.arboriculture.genetics;

import forestry.api.arboriculture.IArboristTracker;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.ISpecies;
import forestry.core.genetics.BreedingTracker;

public class ArboristTracker extends BreedingTracker implements IArboristTracker {
	public ArboristTracker() {
		super(ForestrySpeciesTypes.TREE);
	}

	@Override
	public void registerPickup(ISpecies<?> species) {
	}
}
