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

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import forestry.api.core.IErrorLogicSource;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

public record PacketErrorUpdate(BlockPos pos, short[] errorStates) implements IForestryPacketClient {
	public PacketErrorUpdate(BlockEntity tile, IErrorLogicSource errorLogicSource) {
		this(tile.getBlockPos(), errorLogicSource.getErrorLogic().toArray());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.ERROR_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		NetworkUtil.writeShortArray(buffer, this.errorStates);
	}

	public static PacketErrorUpdate decode(FriendlyByteBuf buffer) {
		BlockPos pos = buffer.readBlockPos();
		short[] errorStats = NetworkUtil.readShortArray(buffer);
		return new PacketErrorUpdate(pos, errorStats);
	}

	public static void handle(PacketErrorUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, IErrorLogicSource.class, errorSourceTile -> errorSourceTile.getErrorLogic().fromArray(msg.errorStates));
	}
}
