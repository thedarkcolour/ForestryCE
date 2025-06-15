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

import forestry.core.circuits.SolderingIronMenu;
import forestry.core.circuits.ISolderingIron;
import forestry.core.inventory.ItemInventorySolderingIron;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;

public class ItemSolderingIron extends ItemWithGui implements ISolderingIron {
	public ItemSolderingIron() {
		super(new Item.Properties().durability(5));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, int slotIndex) {
		return new SolderingIronMenu(windowId, playerInv, new ItemInventorySolderingIron(heldItem));
	}
}
