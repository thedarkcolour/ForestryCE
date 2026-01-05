package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.IItemStackDisplay;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record PacketItemStackDisplay(BlockPos pos, ItemStack itemStack) implements IForestryPacketClient {
	public <T extends TileForestry & IItemStackDisplay> PacketItemStackDisplay(T tile, ItemStack itemStack) {
		this(tile.getBlockPos(), itemStack);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeItem(this.itemStack);
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
