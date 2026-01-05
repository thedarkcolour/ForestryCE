package forestry.cultivation.proxy;

import forestry.api.client.IClientModuleHandler;
import forestry.cultivation.features.CultivationBlocks;
import forestry.cultivation.features.CultivationMenuTypes;
import forestry.cultivation.gui.GuiPlanter;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CultivationClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(CultivationClientHandler::onClientSetup);
	}

	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			CultivationBlocks.MANAGED_PLANTER.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			CultivationBlocks.MANUAL_PLANTER.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			MenuScreens.register(CultivationMenuTypes.PLANTER.menuType(), GuiPlanter::new);
		});
	}
}
