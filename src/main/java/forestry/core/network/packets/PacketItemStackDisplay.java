package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.IItemStackDisplay;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketItemStackDisplay(BlockPos pos, ItemStack stack) implements IForestryPacketClient {
	public <T extends TileForestry & IItemStackDisplay> PacketItemStackDisplay(T tile, ItemStack itemStack) {
		this(tile.getBlockPos(), itemStack);
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketItemStackDisplay msg) {
		buffer.writeBlockPos(msg.pos);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, msg.stack);
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.ITEMSTACK_DISPLAY;
	}

	public static PacketItemStackDisplay decode(RegistryFriendlyByteBuf buffer) {
		return new PacketItemStackDisplay(buffer.readBlockPos(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
	}

	public static void handle(PacketItemStackDisplay msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, IItemStackDisplay.class, tile -> tile.handleItemStackForDisplay(msg.stack));
	}
}
