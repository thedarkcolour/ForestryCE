package forestry.worktable.network.packets;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.tiles.WorktableTile;

public record PacketWorktableRecipeUpdate(BlockPos pos, @Nullable MemorizedRecipe recipe) implements IForestryPacketClient {
	public PacketWorktableRecipeUpdate(WorktableTile tile) {
		this(tile.getBlockPos(), tile.getCurrentRecipe());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.WORKTABLE_CRAFTING_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		NetworkUtil.writeStreamable(buffer, recipe);
	}

	public static PacketWorktableRecipeUpdate decode(FriendlyByteBuf buffer) {
		return new PacketWorktableRecipeUpdate(buffer.readBlockPos(), NetworkUtil.readStreamable(buffer, MemorizedRecipe::new));
	}

	public static void handle(PacketWorktableRecipeUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, WorktableTile.class, tile -> tile.setCurrentRecipe(msg.recipe));
	}
}
