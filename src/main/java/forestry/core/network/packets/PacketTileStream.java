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
package forestry.core.network.packets;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.IStreamable;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

public class PacketTileStream implements IForestryPacketClient {
	protected final BlockPos pos;
	@Nullable
	protected final IStreamable streamable;
	@Nullable
	protected final FriendlyByteBuf payload;

	public <T extends BlockEntity & IStreamable> PacketTileStream(T streamable) {
		this.pos = streamable.getBlockPos();
		this.streamable = streamable;
		this.payload = null;
	}

	private PacketTileStream(BlockPos pos, FriendlyByteBuf payload) {
		this.pos = pos;
		this.streamable = null;
		this.payload = payload;
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.TILE_FORESTRY_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		NetworkUtil.writePayloadBuffer(buffer, streamable::writeData);
	}

	public static PacketTileStream decode(FriendlyByteBuf data) {
		return new PacketTileStream(data.readBlockPos(), NetworkUtil.readPayloadBuffer(data));
	}

	public static void handle(PacketTileStream msg, Player player) {
		IStreamable tile = TileUtil.getTile(player.level(), msg.pos, IStreamable.class);

		if (tile != null) {
			tile.readData(msg.payload);
		}
	}
}
