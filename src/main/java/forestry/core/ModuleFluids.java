package forestry.core;

import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;

@ForestryModule
public class ModuleFluids extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.FLUIDS;
	}
}
