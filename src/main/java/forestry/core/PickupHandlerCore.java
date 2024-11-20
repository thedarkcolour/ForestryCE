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
package forestry.core;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.capability.IIndividualHandlerItem;

public class PickupHandlerCore {
	public static void onItemPickup(Player player, ItemEntity entity) {
		ItemStack stack = entity.getItem();

		if (!stack.isEmpty()) {
			IIndividualHandlerItem.ifPresent(stack, individual -> {
				IBreedingTracker tracker = individual.getType().getBreedingTracker(entity.level(), player.getGameProfile());
				tracker.registerPickup(individual.getSpecies());
			});
		}
	}
}
