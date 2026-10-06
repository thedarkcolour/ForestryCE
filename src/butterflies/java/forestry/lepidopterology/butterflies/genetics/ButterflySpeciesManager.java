package forestry.lepidopterology.butterflies.genetics;

import forestry.core.engine.genetics.SpeciesManager;

public final class ButterflySpeciesManager extends SpeciesManager<ButterflySpeciesDefinition> {
	public static final ButterflySpeciesManager INSTANCE = new ButterflySpeciesManager();

	private ButterflySpeciesManager() {
		super("butterfly_species", ButterflySpeciesDefinition::codec, LepidopterologyReloadHandler::rebuildButterflySpecies);
	}
}
