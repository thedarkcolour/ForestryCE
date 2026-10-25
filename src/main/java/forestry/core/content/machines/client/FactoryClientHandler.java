package forestry.core.content.machines.client;

import forestry.api.client.IClientModuleHandler;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.gui.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class FactoryClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(FactoryClientHandler::registerMenuScreens);
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(FactoryMenuTypes.BOTTLER.menuType(), BottlerScreen::new);
		event.register(FactoryMenuTypes.CARPENTER.menuType(), CarpenterScreen::new);
		event.register(FactoryMenuTypes.CENTRIFUGE.menuType(), CentrifugeScreen::new);
		event.register(FactoryMenuTypes.FABRICATOR.menuType(), FabricatorScreen::new);
		event.register(FactoryMenuTypes.FERMENTER.menuType(), FermenterScreen::new);
		event.register(FactoryMenuTypes.MOISTENER.menuType(), MoistenerScreen::new);
		event.register(FactoryMenuTypes.SMELTER.menuType(), SmelterScreen::new);
		event.register(FactoryMenuTypes.SQUEEZER.menuType(), SqueezerScreen::new);
		event.register(FactoryMenuTypes.STILL.menuType(), StillScreen::new);
	}
}
