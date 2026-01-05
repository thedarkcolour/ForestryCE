package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.circuits.ISocketable;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

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
		buffer.writeBlockPos(this.pos);
		NetworkUtil.writeItemStacks(buffer, this.itemStacks);
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
