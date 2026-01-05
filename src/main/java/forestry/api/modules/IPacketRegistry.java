package forestry.api.modules;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IPacketRegistry {
	/**
	 * Register a packet during the play phase that is handled on the main server thread when the sender is not null.
	 */
	<P extends IForestryPacketServer> void serverbound(ResourceLocation id, Class<P> packetClass, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> packetHandler);

	/**
	 * Register a packet during the play phase that is handled on the main render thread on the client.
	 */
	<P extends IForestryPacketClient> void clientbound(ResourceLocation id, Class<P> packetClass, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, Player> packetHandler);
}
