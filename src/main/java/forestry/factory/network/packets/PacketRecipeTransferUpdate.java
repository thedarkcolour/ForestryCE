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

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.factory.tiles.TileCarpenter;
import forestry.factory.tiles.TileFabricator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRecipeTransferUpdate(BlockPos pos,
										 NonNullList<ItemStack> craftingInventory) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.RECIPE_TRANSFER_UPDATE;
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		NetworkUtil.writeItemStacks(buffer, this.craftingInventory);
	}

	public static PacketRecipeTransferUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRecipeTransferUpdate(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}

	public static void handle(PacketRecipeTransferUpdate msg, IPayloadContext ctx) {
		BlockEntity tile = TileUtil.getTile(player.level(), msg.pos);
		if (tile instanceof TileCarpenter carpenter) {
			int index = 0;
			for (ItemStack stack : msg.craftingInventory) {
				carpenter.getCraftingInventory().setItem(index, stack);
				index++;
			}
		} else if (tile instanceof TileFabricator fabricator) {
			int index = 0;
			for (ItemStack stack : msg.craftingInventory) {
				fabricator.getCraftingInventory().setItem(index, stack);
				index++;
			}
		}
	}
}
