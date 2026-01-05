package forestry.farming;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.farming.client.FarmingClientHandler;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@ForestryModule
public class ModuleFarming extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.FARMING;
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new FarmingClientHandler());
	}
}
