package forestry.core.platform.network;

import forestry.api.modules.IPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;

public class PacketRegistry implements IPacketRegistry {
	private final PayloadRegistrar registrar;

	public PacketRegistry(PayloadRegistrar registrar) {
		this.registrar = registrar;
	}

	@Override
	public <P extends CustomPacketPayload> void serverbound(CustomPacketPayload.Type<P> type, StreamEncoder<RegistryFriendlyByteBuf, P> encoder, StreamDecoder<RegistryFriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> packetHandler) {
		this.registrar.playToServer(type, StreamCodec.of(encoder, decoder), (msg, context) -> handleServerbound(msg, context, packetHandler));
	}

	@Override
	public <P extends CustomPacketPayload> void clientbound(CustomPacketPayload.Type<P> type, StreamEncoder<RegistryFriendlyByteBuf, P> encoder, StreamDecoder<RegistryFriendlyByteBuf, P> decoder, BiConsumer<P, Player> packetHandler) {
		this.registrar.playToClient(type, StreamCodec.of(encoder, decoder), (msg, context) -> handleClientbound(msg, context, packetHandler));
	}

	private static <P extends CustomPacketPayload> void handleServerbound(P message, IPayloadContext context, BiConsumer<P, ServerPlayer> handler) {
		handler.accept(message, (ServerPlayer) context.player());
	}

	private static <P extends CustomPacketPayload> void handleClientbound(P message, IPayloadContext context, BiConsumer<P, Player> handler) {
		handler.accept(message, context.player());
	}
}
