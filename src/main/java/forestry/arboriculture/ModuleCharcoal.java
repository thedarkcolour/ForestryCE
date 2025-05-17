package forestry.arboriculture;

import net.minecraft.resources.ResourceLocation;

import forestry.api.IForestryApi;
import forestry.api.arboriculture.TreeManager;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.modules.BlankForestryModule;

// todo: merge into arboriculture in 1.21
@ForestryModule
public class ModuleCharcoal extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.CHARCOAL;
	}

	@Override
	public void setupApi() {
		TreeManager.charcoalManager = IForestryApi.INSTANCE.getTreeManager().getCharcoalManager();
	}
}
