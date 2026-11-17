package forestry.core.content.energy.client;

import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.features.EnergyMenus;
import forestry.core.content.energy.screen.BiogasEngineScreen;
import forestry.core.content.energy.screen.CombustionEngineScreen;
import forestry.core.content.energy.screen.PeatEngineScreen;
import forestry.core.content.energy.screen.SolarEngineScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class EnergyClientHandler implements forestry.api.client.IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(EnergyClientHandler::registerMenuScreens);
		modBus.addListener(EnergyClientHandler::onClientSetup);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(EnergyBlocks.SOLAR_PANEL.block(), RenderType.cutout()));
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(EnergyMenus.BIOGAS_ENGINE.menuType(), BiogasEngineScreen::new);
		event.register(EnergyMenus.COMBUSTION_ENGINE.menuType(), CombustionEngineScreen::new);
		event.register(EnergyMenus.PEAT_ENGINE.menuType(), PeatEngineScreen::new);
		event.register(EnergyMenus.SOLAR_ENGINE.menuType(), SolarEngineScreen::new);
	}
}
