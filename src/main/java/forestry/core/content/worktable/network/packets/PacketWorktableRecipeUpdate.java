package forestry.core.content.worktable.network.packets;

import forestry.core.content.worktable.recipes.MemorizedRecipe;
import forestry.core.content.worktable.tiles.WorktableBlockEntity;
import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public record PacketWorktableRecipeUpdate(BlockPos pos,
                                          @Nullable MemorizedRecipe recipe) implements CustomPacketPayload {
	public PacketWorktableRecipeUpdate(WorktableBlockEntity tile) {
		this(tile.getBlockPos(), tile.getCurrentRecipe());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdClient.WORKTABLE_CRAFTING_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketWorktableRecipeUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeStreamable(buffer, msg.recipe);
	}

	public static PacketWorktableRecipeUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketWorktableRecipeUpdate(buffer.readBlockPos(), NetworkUtil.readStreamable(buffer, MemorizedRecipe::new));
	}

	public static void handle(PacketWorktableRecipeUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, WorktableBlockEntity.class, tile -> tile.setCurrentRecipe(msg.recipe));
	}
}
