/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.cultivation.proxy;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import forestry.cultivation.features.CultivationBlocks;
import forestry.api.client.IClientModuleHandler;
import forestry.cultivation.features.CultivationMenuTypes;
import forestry.cultivation.gui.GuiPlanter;

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
