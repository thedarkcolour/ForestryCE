package forestry.apiculture.network.packets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.apiculture.bees.genetics.ApicultureReloadHandler;
import forestry.apiculture.bees.genetics.BeeEffectManager;
import forestry.apiculture.network.ApiculturePacketIds;

/**
 * Server -&gt; client sync of the datapack-loaded bee effects, sent on player login/reload (see {@code ModuleCore}'s
 * {@code OnDatapackSyncEvent} listener), <em>before</em> {@link BeeSpeciesSyncPacket} so the effect map exists when the
 * client projects species that reference effect alleles by id. The client has no datapack access to {@code bee_effect}
 * JSON, so this packet is its only source: {@link #handle} stores the effects into the client's
 * {@link BeeEffectManager} mirror and merges them onto the code builtins in the live bee species type.
 * <p>
 * Effects are serialized with a stream codec derived from {@link IBeeEffect#CODEC} (the same dispatch codec used to
 * read the JSON), so no per-primitive stream codecs are needed; sync happens only on login/reload, so the NBT-based
 * encoding is not a hot path.
 */
public record BeeEffectSyncPacket(Map<ResourceLocation, IBeeEffect> effects) implements CustomPacketPayload {
	private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, IBeeEffect>> STREAM_CODEC =
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodecWithRegistries(IBeeEffect.CODEC));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ApiculturePacketIds.BEE_EFFECT_SYNC;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, BeeEffectSyncPacket msg) {
		STREAM_CODEC.encode(buffer, msg.effects);
	}

	public static BeeEffectSyncPacket decode(RegistryFriendlyByteBuf buffer) {
		return new BeeEffectSyncPacket(STREAM_CODEC.decode(buffer));
	}

	public static void handle(BeeEffectSyncPacket msg, Player player) {
		// On an integrated server the client shares the very BeeSpeciesType / BeeEffectManager singletons the server's
		// datapack reload already populated, so re-applying would be redundant (and, mirroring BeeSpeciesSyncPacket,
		// we avoid touching shared state). A true remote client has its own singletons and must apply the sync.
		if (Minecraft.getInstance().hasSingleplayerServer()) {
			return;
		}
		BeeEffectManager.INSTANCE.setDefinitions(msg.effects);
		ApicultureReloadHandler.rebuildBeeEffects(msg.effects);
	}
}
