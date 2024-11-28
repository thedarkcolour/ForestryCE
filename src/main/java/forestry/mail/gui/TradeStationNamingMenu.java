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
import net.minecraft.world.entity.player.Player;

import forestry.api.mail.IMailAddress;
import forestry.core.gui.ContainerTile;
import forestry.core.tiles.TileUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.tiles.TradeStationBlockEntity;

public class TradeStationNamingMenu extends ContainerTile<TradeStationBlockEntity> {
	private final Player player;

	public static TradeStationNamingMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TradeStationBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TradeStationBlockEntity.class);
		return new TradeStationNamingMenu(windowId, inv.player, tile);
	}

	public TradeStationNamingMenu(int windowId, Player player, TradeStationBlockEntity tile) {
		super(windowId, MailMenuTypes.TRADE_NAME.menuType(), tile);
		this.player = player;
	}

	public IMailAddress getAddress() {
		return tile.getAddress();
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (tile.isLinked()) {
			if (this.player instanceof ServerPlayer player) {
				tile.openGui(player, InteractionHand.MAIN_HAND, tile.getBlockPos());
			}
		}
	}
}
