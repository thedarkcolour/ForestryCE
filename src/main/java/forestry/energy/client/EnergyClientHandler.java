package forestry.energy.client;

import forestry.energy.features.EnergyMenus;
import forestry.energy.screen.BiogasEngineScreen;
import forestry.energy.screen.PeatEngineScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class EnergyClientHandler implements forestry.api.client.IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(EnergyClientHandler::registerMenuScreens);
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(EnergyMenus.ENGINE_BIOGAS.menuType(), BiogasEngineScreen::new);
		event.register(EnergyMenus.ENGINE_PEAT.menuType(), PeatEngineScreen::new);
	}
}
