package forestry.factory.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.factory.tiles.TileCarpenter;
import forestry.factory.tiles.TileFabricator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketRecipeTransferRequest(BlockPos pos,
										  NonNullList<ItemStack> craftingInventory) implements IForestryPacketServer {
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

			NetworkUtil.sendNetworkPacket(new PacketRecipeTransferUpdate(carpenter.getBlockPos(), craftingInventory), pos, player.level());
		} else if (tile instanceof TileFabricator fabricator) {
			int index = 0;
			for (ItemStack stack : craftingInventory) {
				fabricator.getCraftingInventory().setItem(index, stack);
				index++;
			}

			NetworkUtil.sendNetworkPacket(new PacketRecipeTransferUpdate(fabricator.getBlockPos(), craftingInventory), pos, player.level());
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		NetworkUtil.writeItemStacks(buffer, this.craftingInventory);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.RECIPE_TRANSFER_REQUEST;
	}

	public static PacketRecipeTransferRequest decode(FriendlyByteBuf buffer) {
		return new PacketRecipeTransferRequest(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}
}
