package forestry.apiculture.bees.genetics;

import forestry.api.apiculture.IBeeJubilance;
import forestry.core.engine.genetics.DefinitionManager;

public final class BeeJubilanceManager extends DefinitionManager<IBeeJubilance> {
	public static final BeeJubilanceManager INSTANCE = new BeeJubilanceManager();

	private BeeJubilanceManager() {
		super("bee_jubilance", IBeeJubilance.CODEC, ApicultureReloadHandler::rebuildJubilances);
	}
}
