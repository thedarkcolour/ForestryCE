package forestry.compat;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IForestryModule;
import forestry.compat.curios.CuriosCompat;
import forestry.compat.curios.client.CuriosClientHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@ForestryModule
public class ModuleCurios implements IForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.CURIOS;
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		if (CuriosCompat.IS_LOADED) {
			registrar.accept(new CuriosClientHandler());
		}
	}
}
