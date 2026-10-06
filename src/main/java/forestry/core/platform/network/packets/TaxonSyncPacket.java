package forestry.core.platform.network.packets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.core.engine.genetics.TaxonManager;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.engine.genetics.TaxonDefinition;
import forestry.core.platform.network.PacketIdClient;

public record TaxonSyncPacket(Map<ResourceLocation, TaxonDefinition> definitions) implements CustomPacketPayload {
	private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, TaxonDefinition>> STREAM_CODEC =
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, TaxonDefinition.STREAM_CODEC);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdClient.TAXON_SYNC;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, TaxonSyncPacket msg) {
		STREAM_CODEC.encode(buffer, msg.definitions);
	}

	public static TaxonSyncPacket decode(RegistryFriendlyByteBuf buffer) {
		return new TaxonSyncPacket(STREAM_CODEC.decode(buffer));
	}

	public static void handle(TaxonSyncPacket msg, Player player) {
		if (Minecraft.getInstance().hasSingleplayerServer()) {
			return;
		}
		TaxonManager.INSTANCE.setDefinitions(msg.definitions);
		GeneticsReloadHandler.rebuildTaxa(msg.definitions.values());
	}
}
