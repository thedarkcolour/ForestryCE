package forestry.apiimpl.client;

import forestry.api.apiculture.bee.IBeeSpecies;
import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.genetics.ILifeStage;
import net.minecraft.resources.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.Map;

public class BeeClientManager implements IBeeClientManager {
	private final IdentityHashMap<ILifeStage, Map<IBeeSpecies, ResourceLocation>> beeModels;

	public BeeClientManager(IdentityHashMap<ILifeStage, Map<IBeeSpecies, ResourceLocation>> beeModels) {
		this.beeModels = beeModels;
	}

	@Override
	public Map<IBeeSpecies, ResourceLocation> getBeeModels(ILifeStage stage) {
		return this.beeModels.get(stage);
	}
}
