package forestry.worktable.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.screens.WorktableMenu;
import forestry.worktable.tiles.WorktableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketWorktableRecipeRequest(BlockPos pos, MemorizedRecipe recipe) implements IForestryPacketServer {
	@Override
	public Type<?> type() {
		return PacketIdServer.WORKTABLE_RECIPE_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketWorktableRecipeRequest msg) {
		buffer.writeBlockPos(msg.pos);
		msg.recipe.writeData(buffer);
	}

	public static PacketWorktableRecipeRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketWorktableRecipeRequest(buffer.readBlockPos(), new MemorizedRecipe(buffer));
	}

	public static void handle(PacketWorktableRecipeRequest msg, IPayloadContext ctx) {
		BlockPos pos = msg.pos();
		MemorizedRecipe recipe = msg.recipe();
		ServerPlayer player = (ServerPlayer) ctx.player();
		ServerLevel level = player.serverLevel();

		TileUtil.actOnTile(level, pos, WorktableTile.class, worktable -> {
			worktable.setCurrentRecipe(recipe);

			if (player.containerMenu instanceof WorktableMenu containerWorktable) {
				containerWorktable.updateCraftMatrix();
			}

			NetworkUtil.sendToPlayersTrackingPos(new PacketWorktableRecipeUpdate(worktable), pos, level);
		});
	}
}
