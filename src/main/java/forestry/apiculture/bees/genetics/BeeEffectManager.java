package forestry.apiculture.bees.genetics;

import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.core.engine.genetics.DefinitionManager;

public final class BeeEffectManager extends DefinitionManager<IBeeEffect> {
	public static final BeeEffectManager INSTANCE = new BeeEffectManager();

	private BeeEffectManager() {
		super("bee_effect", IBeeEffect.CODEC, ApicultureReloadHandler::rebuildBeeEffects);
	}
}
