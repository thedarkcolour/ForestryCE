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
package forestry.factory.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.factory.tiles.TileCarpenter;
import forestry.factory.tiles.TileFabricator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketRecipeTransferRequest(BlockPos pos, NonNullList<ItemStack> craftingInventory) implements IForestryPacketServer {
	public static void handle(PacketRecipeTransferRequest msg, ServerPlayer player) {
		BlockPos pos = msg.pos();
		NonNullList<ItemStack> craftingInventory = msg.craftingInventory();

		BlockEntity tile = TileUtil.getTile(player.level(), pos);
		if (tile instanceof TileCarpenter carpenter) {
			int index = 0;
			for (ItemStack stack : craftingInventory) {
				carpenter.getCraftingInventory().setItem(index, stack);
				index++;
			}

			NetworkUtil.sendToPlayersTrackingPos(new PacketRecipeTransferUpdate(carpenter.getBlockPos(), craftingInventory), pos, player.level());
		} else if (tile instanceof TileFabricator fabricator) {
			int index = 0;
			for (ItemStack stack : craftingInventory) {
				fabricator.getCraftingInventory().setItem(index, stack);
				index++;
			}

			NetworkUtil.sendToPlayersTrackingPos(new PacketRecipeTransferUpdate(fabricator.getBlockPos(), craftingInventory), pos, player.level());
		}
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketRecipeTransferRequest msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeItemStacks(buffer, msg.craftingInventory);
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.RECIPE_TRANSFER_REQUEST;
	}

	public static PacketRecipeTransferRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRecipeTransferRequest(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}
}
