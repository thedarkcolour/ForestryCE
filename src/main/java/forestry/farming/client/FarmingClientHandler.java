package forestry.farming.client;

import forestry.api.client.IClientModuleHandler;
import forestry.farming.features.FarmingMenuTypes;
import forestry.farming.gui.GuiFarm;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class FarmingClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(FarmingClientHandler::onClientSetup);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> MenuScreens.register(FarmingMenuTypes.FARM.menuType(), GuiFarm::new));
	}
}
