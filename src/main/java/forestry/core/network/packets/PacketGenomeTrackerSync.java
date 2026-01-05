package forestry.core.network.packets;

import forestry.api.IForestryApi;
import forestry.api.core.ForestryEvent;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpeciesType;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.genetics.BreedingTracker;
import forestry.core.network.PacketIdClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public record PacketGenomeTrackerSync(@Nullable CompoundTag nbt) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.GENOME_TRACKER_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(this.nbt);
	}

	public static PacketGenomeTrackerSync decode(FriendlyByteBuf buffer) {
		return new PacketGenomeTrackerSync(buffer.readNbt());
	}

	public static void handle(PacketGenomeTrackerSync msg, Player player) {
		if (msg.nbt != null) {
			String type = msg.nbt.getString(BreedingTracker.TYPE_KEY);
			ISpeciesType<?, ?> root = IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypeSafe(new ResourceLocation(type));

			if (root != null) {
				IBreedingTracker tracker = root.getBreedingTracker(player.getCommandSenderWorld(), player.getGameProfile());
				tracker.readFromNbt(msg.nbt);
				MinecraftForge.EVENT_BUS.post(new ForestryEvent.SyncedBreedingTracker(tracker, player));
			}
		}
	}
}
