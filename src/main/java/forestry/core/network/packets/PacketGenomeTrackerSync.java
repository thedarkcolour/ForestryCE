package forestry.core.network.packets;

import forestry.api.IForestryApi;
import forestry.api.event.BreedingTrackerEvent;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpeciesType;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.genetics.BreedingTracker;
import forestry.core.network.PacketIdClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public record PacketGenomeTrackerSync(@Nullable CompoundTag nbt) implements IForestryPacketClient {
	@Override
	public Type<?> type() {
		return PacketIdClient.GENOME_TRACKER_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketGenomeTrackerSync msg) {
		buffer.writeNbt(msg.nbt);
	}

	public static PacketGenomeTrackerSync decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGenomeTrackerSync(buffer.readNbt());
	}

	public static void handle(PacketGenomeTrackerSync msg, IPayloadContext ctx) {
		if (msg.nbt != null) {
			ISpeciesType<?, ?> type = IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypeSafe(ResourceLocation.parse(msg.nbt.getString(BreedingTracker.TYPE_KEY)));

			if (type != null) {
				Player player = ctx.player();
				IBreedingTracker tracker = type.getBreedingTracker(player.getCommandSenderWorld(), player.getGameProfile());
				tracker.readFromNbt(msg.nbt, player.registryAccess());
				NeoForge.EVENT_BUS.post(new BreedingTrackerEvent.Synced(type, tracker, player));
			}
		}
	}
}
