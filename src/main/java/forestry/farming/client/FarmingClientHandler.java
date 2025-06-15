package forestry.farming.client;

import forestry.api.client.IClientModuleHandler;
import forestry.farming.features.FarmingMenuTypes;
import forestry.farming.gui.GuiFarm;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class FarmingClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(FarmingClientHandler::registerMenus);
	}

	private static void registerMenus(RegisterMenuScreensEvent event) {
		event.register(FarmingMenuTypes.FARM.menuType(), GuiFarm::new);
	}
}
