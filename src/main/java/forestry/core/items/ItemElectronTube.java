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
package forestry.core.items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import forestry.api.IForestryApi;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitLayout;
import forestry.api.circuits.ICircuitManager;
import forestry.api.core.ItemGroups;
import forestry.core.utils.ItemTooltipUtil;

import it.unimi.dsi.fastutil.Pair;

public class ItemElectronTube extends ItemOverlay {
	public ItemElectronTube(ItemOverlay.IOverlayInfo type) {
		super(ItemGroups.tabForestry, type);
	}

	@Override
	public void appendHoverText(ItemStack itemstack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		ArrayList<Pair<ICircuitLayout, ICircuit>> circuits = getCircuits(itemstack);
		if (!circuits.isEmpty()) {
			if (Screen.hasShiftDown()) {
				for (var entry : circuits) {
					list.add(entry.left().getUsage().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE));
					entry.right().addTooltip(list);
				}
			} else {
				ItemTooltipUtil.addShiftInformation(itemstack, world, list, flag);
			}
		} else {
			list.add(Component.literal("<")
					.append(Component.translatable("for.gui.noeffect")
							.append(">").withStyle(ChatFormatting.GRAY)));
		}
	}

	private static ArrayList<Pair<ICircuitLayout, ICircuit>> getCircuits(ItemStack stack) {
		ArrayList<Pair<ICircuitLayout, ICircuit>> circuits = new ArrayList<>();
		ICircuitManager manager = IForestryApi.INSTANCE.getCircuitManager();

		for (ICircuitLayout layout : manager.getLayouts()) {
			ICircuit circuit = manager.getCircuit(layout, stack);
			if (circuit != null) {
				circuits.add(Pair.of(layout, circuit));
			}
		}

		return circuits;
	}
}
