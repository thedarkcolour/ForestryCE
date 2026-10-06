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

import forestry.api.apiculture.IBeeJubilance;
import forestry.apiculture.bees.genetics.ApicultureReloadHandler;
import forestry.apiculture.bees.genetics.BeeJubilanceManager;
import forestry.apiculture.network.ApiculturePacketIds;

// must be sent before BeeSpeciesSyncPacket, the client skips a species with an unknown jubilance
public record BeeJubilanceSyncPacket(Map<ResourceLocation, IBeeJubilance> jubilances) implements CustomPacketPayload {
	private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, IBeeJubilance>> STREAM_CODEC =
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodecWithRegistries(IBeeJubilance.CODEC));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ApiculturePacketIds.BEE_JUBILANCE_SYNC;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, BeeJubilanceSyncPacket msg) {
		STREAM_CODEC.encode(buffer, msg.jubilances);
	}

	public static BeeJubilanceSyncPacket decode(RegistryFriendlyByteBuf buffer) {
		return new BeeJubilanceSyncPacket(STREAM_CODEC.decode(buffer));
	}

	public static void handle(BeeJubilanceSyncPacket msg, Player player) {
		// an integrated server shares these singletons with the client
		if (Minecraft.getInstance().hasSingleplayerServer()) {
			return;
		}
		BeeJubilanceManager.INSTANCE.setDefinitions(msg.jubilances);
		ApicultureReloadHandler.rebuildJubilances(msg.jubilances);
	}
}
