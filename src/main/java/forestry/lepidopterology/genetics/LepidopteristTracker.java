package forestry.lepidopterology.genetics;

import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.ISpecies;
import forestry.api.lepidopterology.ILepidopteristTracker;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.core.genetics.BreedingTracker;

public class LepidopteristTracker extends BreedingTracker implements ILepidopteristTracker {
	public LepidopteristTracker() {
		super(ForestrySpeciesTypes.BUTTERFLY);
	}

	@Override
	public void registerCatch(IButterfly butterfly) {
		registerSpecies(butterfly.getSpecies());
		registerSpecies(butterfly.getInactiveSpecies());
	}

	@Override
	public void registerPickup(ISpecies<?> species) {
		registerSpecies(species);
	}
}
