package forestry.worktable.client;

import forestry.api.client.IClientModuleHandler;
import forestry.worktable.features.WorktableMenus;
import forestry.worktable.screens.WorktableScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class WorktableClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(WorktableClientHandler::registerMenuScreens);
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(WorktableMenus.WORKTABLE.menuType(), WorktableScreen::new);
	}
}
