package forestry.core.content.machines.network.packets;

import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.content.machines.tiles.CarpenterBlockEntity;
import forestry.core.content.machines.tiles.FabricatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketRecipeTransferUpdate(BlockPos pos,
										 NonNullList<ItemStack> craftingInventory) implements CustomPacketPayload {
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdClient.RECIPE_TRANSFER_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketRecipeTransferUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeItemStacks(buffer, msg.craftingInventory);
	}

	public static PacketRecipeTransferUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRecipeTransferUpdate(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}

	public static void handle(PacketRecipeTransferUpdate msg, Player player) {
		BlockEntity tile = TileUtil.getTile(player.level(), msg.pos);
		if (tile instanceof CarpenterBlockEntity carpenter) {
			int index = 0;
			for (ItemStack stack : msg.craftingInventory) {
				carpenter.getCraftingInventory().setItem(index, stack);
				index++;
			}
		} else if (tile instanceof FabricatorBlockEntity fabricator) {
			int index = 0;
			for (ItemStack stack : msg.craftingInventory) {
				fabricator.getCraftingInventory().setItem(index, stack);
				index++;
			}
		}
	}
}
