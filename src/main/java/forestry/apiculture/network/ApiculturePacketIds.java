package forestry.apiculture.network;

import forestry.apiculture.network.packets.BeeEffectSyncPacket;
import forestry.apiculture.network.packets.BeeJubilanceSyncPacket;
import forestry.apiculture.network.packets.BeeSpeciesSyncPacket;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.network.packets.PacketHabitatBiomePointer;
import forestry.core.platform.network.PacketIdServer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Packet ids owned by the apiculture module. The path strings are the packets' wire identity and
 * must never change.
 */
public class ApiculturePacketIds {
	public static final CustomPacketPayload.Type<PacketBeeLogicActive> BEE_LOGIC_ACTIVE = PacketIdServer.type("bee_logic_active");
	public static final CustomPacketPayload.Type<PacketHabitatBiomePointer> HABITAT_BIOME_POINTER = PacketIdServer.type("habitat_biome_pointer");
	public static final CustomPacketPayload.Type<BeeSpeciesSyncPacket> BEE_SPECIES_SYNC = PacketIdServer.type("bee_species_sync");
	public static final CustomPacketPayload.Type<BeeEffectSyncPacket> BEE_EFFECT_SYNC = PacketIdServer.type("bee_effect_sync");
	public static final CustomPacketPayload.Type<BeeJubilanceSyncPacket> BEE_JUBILANCE_SYNC = PacketIdServer.type("bee_jubilance_sync");
}
