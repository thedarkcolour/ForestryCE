package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.circuits.ISocketable;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketSocketUpdate(BlockPos pos, List<ItemStack> itemStacks) implements IForestryPacketClient {
	public static <T extends BlockEntity & ISocketable> PacketSocketUpdate create(T tile) {
		BlockPos pos = tile.getBlockPos();

		ArrayList<ItemStack> itemStacks = new ArrayList<>(tile.getSocketCount());
		for (int i = 0; i < tile.getSocketCount(); i++) {
			itemStacks.set(i, tile.getSocket(i));
		}

		return new PacketSocketUpdate(pos, itemStacks);
	}

	public Type<?> type() {
		return PacketIdClient.SOCKET_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketSocketUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeItemStacks(buffer, msg.itemStacks);
	}

	public static PacketSocketUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketSocketUpdate(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}

	public static void handle(PacketSocketUpdate msg, IPayloadContext ctx) {
		if (ctx.player().level().getBlockEntity(msg.pos) instanceof ISocketable socketable) {
			for (int i = 0; i < msg.itemStacks.size(); i++) {
				socketable.setSocket(i, msg.itemStacks.get(i));
			}
		}
	}
}
