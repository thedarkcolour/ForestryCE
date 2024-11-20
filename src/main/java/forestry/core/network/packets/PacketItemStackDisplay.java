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
import net.minecraft.world.item.ItemStack;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.IItemStackDisplay;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TileUtil;

public record PacketItemStackDisplay(BlockPos pos, ItemStack itemStack) implements IForestryPacketClient {
	public <T extends TileForestry & IItemStackDisplay> PacketItemStackDisplay(T tile, ItemStack itemStack) {
		this(tile.getBlockPos(), itemStack);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeItem(itemStack);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.ITEMSTACK_DISPLAY;
	}

	public static PacketItemStackDisplay decode(FriendlyByteBuf buffer) {
		return new PacketItemStackDisplay(buffer.readBlockPos(), buffer.readItem());
	}

	public static void handle(PacketItemStackDisplay msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, IItemStackDisplay.class, tile -> tile.handleItemStackForDisplay(msg.itemStack));
	}
}
