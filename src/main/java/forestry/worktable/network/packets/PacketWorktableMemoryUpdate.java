package forestry.worktable.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.worktable.recipes.RecipeMemory;
import forestry.worktable.tiles.WorktableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketWorktableMemoryUpdate(BlockPos pos, RecipeMemory memory) implements IForestryPacketClient {
	public PacketWorktableMemoryUpdate(WorktableTile worktable) {
		this(worktable.getBlockPos(), worktable.getMemory());
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.WORKTABLE_MEMORY_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketWorktableMemoryUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		msg.memory.writeData(buffer);
	}

	public static PacketWorktableMemoryUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketWorktableMemoryUpdate(buffer.readBlockPos(), new RecipeMemory(buffer));
	}

	public static void handle(PacketWorktableMemoryUpdate msg, IPayloadContext context) {
		WorktableTile tile = TileUtil.getTile(context.player().level(), msg.pos, WorktableTile.class);
		if (tile != null) {
			tile.getMemory().copy(msg.memory);
		}
	}
}
