package forestry.core.platform.network.packets;

import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.tile.IItemStackDisplay;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record PacketItemStackDisplay(BlockPos pos, ItemStack itemStack) implements CustomPacketPayload {
	public <T extends ForestryBlockEntity & IItemStackDisplay> PacketItemStackDisplay(T tile, ItemStack itemStack) {
		this(tile.getBlockPos(), itemStack);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdClient.ITEMSTACK_DISPLAY;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketItemStackDisplay msg) {
		buffer.writeBlockPos(msg.pos);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, msg.itemStack);
	}

	public static PacketItemStackDisplay decode(RegistryFriendlyByteBuf buffer) {
		return new PacketItemStackDisplay(buffer.readBlockPos(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
	}

	public static void handle(PacketItemStackDisplay msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, IItemStackDisplay.class, tile -> tile.handleItemStackForDisplay(msg.itemStack));
	}
}
