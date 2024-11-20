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

import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TileUtil;

public record PacketActiveUpdate(BlockPos pos, boolean active) implements IForestryPacketClient {
	public PacketActiveUpdate(IActivatable tile) {
		this(tile.getCoordinates(), tile.isActive());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.TILE_FORESTRY_ACTIVE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeBoolean(active);
	}

	public static PacketActiveUpdate decode(FriendlyByteBuf buffer) {
		return new PacketActiveUpdate(buffer.readBlockPos(), buffer.readBoolean());
	}

	public static void handle(PacketActiveUpdate msg, Player player) {
		BlockEntity tile = TileUtil.getTile(player.level(), msg.pos);

		if (tile instanceof IActivatable activatable) {
			activatable.setActive(msg.active);
		} else if (tile instanceof IMultiblockComponent component) {
			if (component.getMultiblockLogic().isConnected() && component.getMultiblockLogic().getController() instanceof IActivatable activatable) {
				activatable.setActive(msg.active);
			}
		}
	}
}
