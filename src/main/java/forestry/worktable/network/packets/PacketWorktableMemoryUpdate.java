package forestry.worktable.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.worktable.recipes.RecipeMemory;
import forestry.worktable.tiles.WorktableTile;

public record PacketWorktableMemoryUpdate(BlockPos pos, RecipeMemory memory) implements IForestryPacketClient {
	public PacketWorktableMemoryUpdate(WorktableTile worktable) {
		this(worktable.getBlockPos(), worktable.getMemory());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.WORKTABLE_MEMORY_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		memory.writeData(buffer);
	}

	public static PacketWorktableMemoryUpdate decode(FriendlyByteBuf buffer) {
		return new PacketWorktableMemoryUpdate(buffer.readBlockPos(), new RecipeMemory(buffer));
	}

	public static void handle(PacketWorktableMemoryUpdate msg, Player player) {
		WorktableTile tile = TileUtil.getTile(player.level(), msg.pos, WorktableTile.class);
		if (tile != null) {
			tile.getMemory().copy(msg.memory);
		}
	}
}
