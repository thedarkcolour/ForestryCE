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

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.mail.tiles.TileTrader;

public record PacketTraderAddressRequest(BlockPos pos, String addressName) implements IForestryPacketServer {
	public PacketTraderAddressRequest(TileTrader tile, String addressName) {
		this(tile.getBlockPos(), addressName);
	}

	public static void handle(PacketTraderAddressRequest msg, ServerPlayer player) {
		TileUtil.actOnTile(player.level(), msg.pos(), TileTrader.class, tile -> tile.handleSetAddressRequest(msg.addressName()));
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.TRADING_ADDRESS_REQUEST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeUtf(addressName);
	}

	public static PacketTraderAddressRequest decode(FriendlyByteBuf buffer) {
		return new PacketTraderAddressRequest(buffer.readBlockPos(), buffer.readUtf());
	}
}
