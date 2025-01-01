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
package forestry.mail.network.packets;

import forestry.api.mail.IMailAddress;
import forestry.mail.MailAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.mail.tiles.TileTrader;

public record PacketTraderAddressResponse(BlockPos pos, IMailAddress address) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.TRADING_ADDRESS_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeUtf(address.getName());
	}

	public static PacketTraderAddressResponse decode(FriendlyByteBuf buffer) {
		return new PacketTraderAddressResponse(buffer.readBlockPos(), new MailAddress(buffer.readUtf()));
	}

	public static void handle(PacketTraderAddressResponse msg, Player player) {
		TileUtil.actOnTile(player.level, msg.pos, TileTrader.class, tile -> tile.setAddress(msg.address));
	}
}
