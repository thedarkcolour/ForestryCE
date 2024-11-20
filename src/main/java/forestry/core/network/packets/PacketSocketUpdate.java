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
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import forestry.core.circuits.ISocketable;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

public record PacketSocketUpdate(BlockPos pos, NonNullList<ItemStack> itemStacks) implements IForestryPacketClient {
	public static <T extends BlockEntity & ISocketable> PacketSocketUpdate create(T tile) {
		BlockPos pos = tile.getBlockPos();

		NonNullList<ItemStack> itemStacks = NonNullList.withSize(tile.getSocketCount(), ItemStack.EMPTY);
		for (int i = 0; i < tile.getSocketCount(); i++) {
			itemStacks.set(i, tile.getSocket(i));
		}

		return new PacketSocketUpdate(pos, itemStacks);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.SOCKET_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		NetworkUtil.writeItemStacks(buffer, itemStacks);
	}

	public static PacketSocketUpdate decode(FriendlyByteBuf buffer) {
		return new PacketSocketUpdate(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}

	public static void handle(PacketSocketUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, ISocketable.class, socketable -> {
			for (int i = 0; i < msg.itemStacks.size(); i++) {
				socketable.setSocket(i, msg.itemStacks.get(i));
			}
		});
	}
}
