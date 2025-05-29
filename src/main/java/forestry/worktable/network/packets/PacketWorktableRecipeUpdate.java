package forestry.worktable.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.tiles.WorktableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public record PacketWorktableRecipeUpdate(BlockPos pos,
										  @Nullable MemorizedRecipe recipe) implements IForestryPacketClient {
	public PacketWorktableRecipeUpdate(WorktableTile tile) {
		this(tile.getBlockPos(), tile.getCurrentRecipe());
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.WORKTABLE_CRAFTING_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketWorktableRecipeUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeStreamable(buffer, msg.recipe);
	}

	public static PacketWorktableRecipeUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketWorktableRecipeUpdate(buffer.readBlockPos(), NetworkUtil.readStreamable(buffer, MemorizedRecipe::new));
	}

	public static void handle(PacketWorktableRecipeUpdate msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, WorktableTile.class, tile -> tile.setCurrentRecipe(msg.recipe));
	}
}
