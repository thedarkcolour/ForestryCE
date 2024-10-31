package forestry.worktable.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.screens.WorktableMenu;
import forestry.worktable.tiles.WorktableTile;

public record PacketWorktableRecipeRequest(BlockPos pos, MemorizedRecipe recipe) implements IForestryPacketServer {
	@Override
	public ResourceLocation id() {
		return PacketIdServer.WORKTABLE_RECIPE_REQUEST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		recipe.writeData(buffer);
	}

	public static PacketWorktableRecipeRequest decode(FriendlyByteBuf buffer) {
		return new PacketWorktableRecipeRequest(buffer.readBlockPos(), new MemorizedRecipe(buffer));
	}

	public static void handle(PacketWorktableRecipeRequest msg, ServerPlayer player) {
		BlockPos pos = msg.pos();
		MemorizedRecipe recipe = msg.recipe();
		TileUtil.actOnTile(player.level(), pos, WorktableTile.class, worktable -> {
			worktable.setCurrentRecipe(recipe);

			if (player.containerMenu instanceof WorktableMenu containerWorktable) {
				containerWorktable.updateCraftMatrix();
			}

			NetworkUtil.sendNetworkPacket(new PacketWorktableRecipeUpdate(worktable), pos, player.level());
		});
	}
}