package forestry.agriculture.client;

import forestry.agriculture.multifarm.gui.MultifarmScreen;
import forestry.api.client.IClientModuleHandler;
import forestry.agriculture.features.FarmingBlocks;
import forestry.agriculture.features.FarmingMenuTypes;
import forestry.agriculture.minifarm.gui.MinifarmScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class FarmingClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(FarmingClientHandler::onClientSetup);
		modBus.addListener(FarmingClientHandler::registerMenuScreens);
	}

	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			FarmingBlocks.MANAGED_MINIFARM.getList().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			FarmingBlocks.MANUAL_MINIFARM.getList().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
		});
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(FarmingMenuTypes.MULTIFARM.menuType(), MultifarmScreen::new);
		event.register(FarmingMenuTypes.MINIFARM.menuType(), MinifarmScreen::new);
	}
}
