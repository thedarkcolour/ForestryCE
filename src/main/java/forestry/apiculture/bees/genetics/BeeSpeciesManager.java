package forestry.apiculture.bees.genetics;

import forestry.core.engine.genetics.SpeciesManager;

public final class BeeSpeciesManager extends SpeciesManager<BeeSpeciesDefinition> {
	public static final BeeSpeciesManager INSTANCE = new BeeSpeciesManager();

	private BeeSpeciesManager() {
		super("bee_species", BeeSpeciesDefinition::codec, ApicultureReloadHandler::rebuildSpecies);
	}
}
