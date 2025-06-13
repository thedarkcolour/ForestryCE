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
package forestry.mail.gui;

import forestry.core.gui.TileMenu;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.tiles.TileUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.inventory.InventoryStampCollector;
import forestry.mail.tiles.TileStampCollector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class StampCollectorMenu extends TileMenu<TileStampCollector> {
	public static StampCollectorMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileStampCollector tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileStampCollector.class);
		return new StampCollectorMenu(windowId, inv, tile);
	}

	public StampCollectorMenu(int windowId, Inventory inv, TileStampCollector tile) {
		super(windowId, MailMenuTypes.STAMP_COLLECTOR.menuType(), inv, tile, 8, 111);

		// Filter
		addSlot(new SlotFiltered(tile, InventoryStampCollector.SLOT_FILTER, 80, 19));

		// Collected Stamps
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new SlotOutput(tile, j + i * 9 + InventoryStampCollector.SLOT_BUFFER_1, 8 + j * 18, 46 + i * 18));
			}
		}
	}
}
