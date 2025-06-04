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

import forestry.api.mail.IMailAddress;
import forestry.core.gui.ContainerTile;
import forestry.core.tiles.TileUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.tiles.TileTrader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerTradeName extends ContainerTile<TileTrader> {
	public static ContainerTradeName fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileTrader tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileTrader.class);
		return new ContainerTradeName(windowId, inv.player, tile);
	}

	public ContainerTradeName(int windowId, Player player, TileTrader tile) {
		super(windowId, MailMenuTypes.TRADE_NAME.menuType(), tile, player);
	}

	public IMailAddress getAddress() {
		return this.tile.getAddress();
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (this.tile.isLinked()) {
			for (Object crafter : this.containerListeners) {
				if (crafter instanceof ServerPlayer player) {
                    this.tile.interactNoItem(, player, this.tile.getBlockPos());
				}
			}
		}
	}
}
