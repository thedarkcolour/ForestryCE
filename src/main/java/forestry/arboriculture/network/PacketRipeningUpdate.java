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
package forestry.arboriculture.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.modules.IForestryPacketClient;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;

public record PacketRipeningUpdate(BlockPos pos, int value) implements IForestryPacketClient {
	public PacketRipeningUpdate(TileLeaves leaves) {
		this(leaves.getBlockPos(), leaves.getFruitColour());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.RIPENING_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(value);
	}

	public static PacketRipeningUpdate decode(FriendlyByteBuf buffer) {
		return new PacketRipeningUpdate(buffer.readBlockPos(), buffer.readVarInt());
	}

	public static void handle(PacketRipeningUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, IRipeningPacketReceiver.class, tile -> tile.fromRipeningPacket(msg.value));
	}
}
