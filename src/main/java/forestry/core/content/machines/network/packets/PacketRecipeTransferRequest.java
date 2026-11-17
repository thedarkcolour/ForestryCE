package forestry.core.content.machines.network.packets;

import forestry.core.platform.network.PacketIdServer;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.content.machines.tiles.CarpenterBlockEntity;
import forestry.core.content.machines.tiles.FabricatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketRecipeTransferRequest(BlockPos pos,
										  NonNullList<ItemStack> craftingInventory) implements CustomPacketPayload {
	public static void handle(PacketRecipeTransferRequest msg, ServerPlayer player) {
		BlockPos pos = msg.pos();
		NonNullList<ItemStack> craftingInventory = msg.craftingInventory();

		BlockEntity tile = TileUtil.getTile(player.level(), pos);
		if (tile instanceof CarpenterBlockEntity carpenter) {
			int index = 0;
			for (ItemStack stack : craftingInventory) {
				carpenter.getCraftingInventory().setItem(index, stack);
				index++;
			}

			NetworkUtil.sendNetworkPacket(new PacketRecipeTransferUpdate(carpenter.getBlockPos(), craftingInventory), pos, player.level());
		} else if (tile instanceof FabricatorBlockEntity fabricator) {
			int index = 0;
			for (ItemStack stack : craftingInventory) {
				fabricator.getCraftingInventory().setItem(index, stack);
				index++;
			}

			NetworkUtil.sendNetworkPacket(new PacketRecipeTransferUpdate(fabricator.getBlockPos(), craftingInventory), pos, player.level());
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdServer.RECIPE_TRANSFER_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketRecipeTransferRequest msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeItemStacks(buffer, msg.craftingInventory);
	}

	public static PacketRecipeTransferRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRecipeTransferRequest(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}
}
