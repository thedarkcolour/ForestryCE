package forestry.core.content.sorting.client;

import forestry.api.client.IClientModuleHandler;
import forestry.core.content.sorting.features.SortingMenuTypes;
import forestry.core.content.sorting.gui.GeneticFilterScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class SortingClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(SortingClientHandler::registerMenuScreens);
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(SortingMenuTypes.GENETIC_FILTER.menuType(), GeneticFilterScreen::new);
	}
}
