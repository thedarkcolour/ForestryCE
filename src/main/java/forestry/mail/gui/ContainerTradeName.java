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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;

import forestry.api.mail.IMailAddress;
import forestry.core.gui.ContainerTile;
import forestry.core.tiles.TileUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.tiles.TileTrader;

public class ContainerTradeName extends ContainerTile<TileTrader> {
	public static ContainerTradeName fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileTrader tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileTrader.class);
		return new ContainerTradeName(windowId, inv, tile);
	}

	public ContainerTradeName(int windowId, Inventory inv, TileTrader tile) {
		super(windowId, MailMenuTypes.TRADE_NAME.menuType(), tile);
	}

	public IMailAddress getAddress() {
		return tile.getAddress();
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (tile.isLinked()) {
			for (Object crafter : containerListeners) {
				if (crafter instanceof ServerPlayer player) {
					tile.openGui(player, InteractionHand.MAIN_HAND, tile.getBlockPos());
				}
			}
		}
	}
}
