package forestry.arboriculture.trees.genetics;

import forestry.core.engine.genetics.SpeciesManager;

public final class TreeSpeciesManager extends SpeciesManager<TreeSpeciesDefinition> {
	public static final TreeSpeciesManager INSTANCE = new TreeSpeciesManager();

	private TreeSpeciesManager() {
		super("tree_species", TreeSpeciesDefinition::codec, ArboricultureReloadHandler::rebuildTreeSpecies);
	}
}
