package forestry.cultivation.proxy;

import forestry.api.client.IClientModuleHandler;
import forestry.cultivation.features.CultivationBlocks;
import forestry.cultivation.features.CultivationMenuTypes;
import forestry.cultivation.gui.GuiPlanter;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class CultivationClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(CultivationClientHandler::onClientSetup);
		modBus.addListener(CultivationClientHandler::registerMenuScreens);
	}

	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			// todo move to model JSON
			CultivationBlocks.MANAGED_PLANTER.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			CultivationBlocks.MANUAL_PLANTER.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
		});
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(CultivationMenuTypes.PLANTER.menuType(), GuiPlanter::new);
	}
}
