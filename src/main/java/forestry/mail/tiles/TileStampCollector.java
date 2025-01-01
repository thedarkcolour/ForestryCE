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
package forestry.mail.tiles;

import forestry.mail.PostOffice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.mail.IStamps;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.tiles.TileBase;
import forestry.core.utils.InventoryUtil;
import forestry.mail.features.MailTiles;
import forestry.mail.gui.ContainerStampCollector;
import forestry.mail.inventory.InventoryStampCollector;

public class TileStampCollector extends TileBase implements Container {
	public TileStampCollector(BlockPos pos, BlockState state) {
		super(MailTiles.STAMP_COLLECTOR.tileType(), pos, state);
		setInternalInventory(new InventoryStampCollector(this));
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (!updateOnInterval(20)) {
			return;
		}

		ItemStack stamp = null;

		IInventoryAdapter inventory = getInternalInventory();
		if (inventory.getItem(InventoryStampCollector.SLOT_FILTER).isEmpty()) {
			stamp = PostOffice.getOrCreate((ServerLevel) level).getAnyStamp(1);
		} else {
			ItemStack filter = inventory.getItem(InventoryStampCollector.SLOT_FILTER);
			if (filter.getItem() instanceof IStamps) {
				stamp = PostOffice.getOrCreate((ServerLevel) level).getAnyStamp(((IStamps) filter.getItem()).getPostage(filter), 1);
			}
		}

		if (stamp == null) {
			return;
		}

		// Store it.
		InventoryUtil.stowInInventory(stamp, inventory, true, InventoryStampCollector.SLOT_BUFFER_1, InventoryStampCollector.SLOT_BUFFER_COUNT);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerStampCollector(windowId, inv, this);
	}
}
