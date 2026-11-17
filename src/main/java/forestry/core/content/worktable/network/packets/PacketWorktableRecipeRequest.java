package forestry.core.content.worktable.network.packets;

import forestry.core.platform.network.PacketIdServer;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.content.worktable.recipes.MemorizedRecipe;
import forestry.core.content.worktable.screens.WorktableMenu;
import forestry.core.content.worktable.tiles.WorktableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record PacketWorktableRecipeRequest(BlockPos pos, MemorizedRecipe recipe) implements CustomPacketPayload {
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdServer.WORKTABLE_RECIPE_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketWorktableRecipeRequest msg) {
		buffer.writeBlockPos(msg.pos);
		msg.recipe.writeData(buffer);
	}

	public static PacketWorktableRecipeRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketWorktableRecipeRequest(buffer.readBlockPos(), new MemorizedRecipe(buffer));
	}

	public static void handle(PacketWorktableRecipeRequest msg, ServerPlayer player) {
		BlockPos pos = msg.pos();
		MemorizedRecipe recipe = msg.recipe();
		TileUtil.actOnTile(player.level(), pos, WorktableBlockEntity.class, worktable -> {
			worktable.setCurrentRecipe(recipe);

			if (player.containerMenu instanceof WorktableMenu containerWorktable) {
				containerWorktable.updateCraftMatrix();
			}

			NetworkUtil.sendNetworkPacket(new PacketWorktableRecipeUpdate(worktable), pos, player.level());
		});
	}
}
