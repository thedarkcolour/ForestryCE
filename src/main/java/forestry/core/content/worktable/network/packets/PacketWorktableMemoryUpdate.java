package forestry.core.content.worktable.network.packets;

import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.worktable.recipes.RecipeMemory;
import forestry.core.content.worktable.tiles.WorktableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record PacketWorktableMemoryUpdate(BlockPos pos, RecipeMemory memory) implements CustomPacketPayload {
	public PacketWorktableMemoryUpdate(WorktableBlockEntity worktable) {
		this(worktable.getBlockPos(), worktable.getMemory());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdClient.WORKTABLE_MEMORY_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketWorktableMemoryUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		msg.memory.writeData(buffer);
	}

	public static PacketWorktableMemoryUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketWorktableMemoryUpdate(buffer.readBlockPos(), new RecipeMemory(buffer));
	}

	public static void handle(PacketWorktableMemoryUpdate msg, Player player) {
		WorktableBlockEntity tile = TileUtil.getTile(player.level(), msg.pos, WorktableBlockEntity.class);
		if (tile != null) {
			tile.getMemory().copy(msg.memory);
		}
	}
}
