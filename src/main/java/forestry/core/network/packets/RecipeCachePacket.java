package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.recipes.RecipeManagers;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RecipeCachePacket implements IForestryPacketClient {
	@Override
	public Type<?> type() {
		return PacketIdClient.RECIPE_CACHE;
	}

	public static void handle(RecipeCachePacket msg, IPayloadContext ctx) {
		RecipeManagers.invalidateCaches();
	}
}
