package forestry.core.genetics;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import forestry.api.IForestryApi;
import forestry.api.event.BreedingTrackerEvent;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.core.network.packets.PacketGenomeTrackerSync;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class BreedingTracker extends SavedData implements IBreedingTracker {
	private static final String SPECIES_KEY = "SD";
	private static final String MUTATIONS_KEY = "MD";
	private static final String RESEARCHED_KEY = "RD";
	private static final String MUTATION_FORMAT = "%s-%s=%s";

	// used for network deserialization
	public static final String TYPE_KEY = "TYPE";

	private final ResourceLocation typeId;
	private final Set<ResourceLocation> discoveredSpecies = new HashSet<>();
	private final Set<String> discoveredMutations = new HashSet<>();
	private final Set<String> researchedMutations = new HashSet<>();

	@Nullable
	private ResolvableProfile username;
	@Nullable
	private Level level;

	protected BreedingTracker(ResourceLocation typeId) {
		this.typeId = typeId;
	}

	public void setUsername(@Nullable ResolvableProfile username) {
		this.username = username;
	}

	public void setLevel(@Nullable Level level) {
		this.level = level;
	}

	@Override
	public void syncToPlayer(Player player) {
		if (player instanceof ServerPlayer && !player.isFakePlayer()) {
			CompoundTag nbt = new CompoundTag();
			writeToNbt(nbt);
			PacketGenomeTrackerSync packet = new PacketGenomeTrackerSync(nbt);
            PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
        }
	}

	// Sends the given species and mutations to client. Use to sync serverside breeding updates to the client.
	private void sendUpdate(Collection<ResourceLocation> discoveredSpecies, Collection<String> discoveredMutations, Collection<String> researchedMutations) {
		if (this.level != null && this.username != null && this.username.id().isPresent()) {
			Player player = this.level.getPlayerByUUID(this.username.id().get());

			if (player instanceof ServerPlayer && !player.isFakePlayer()) {
				CompoundTag nbt = new CompoundTag();
				writeAllValues(nbt, discoveredSpecies, discoveredMutations, researchedMutations);
				writeUpdateData(nbt);
				PacketGenomeTrackerSync packet = new PacketGenomeTrackerSync(nbt);
                PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
            }
		}
	}

	@Override
	public final CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
		writeToNbt(nbt);
		return nbt;
	}

	/* HELPER FUNCTIONS TO PREVENT OBFUSCATION OF INTERFACE METHODS */
	@OverridingMethodsMustInvokeSuper
	@Override
	public void writeToNbt(CompoundTag nbt) {
		writeAllValues(nbt, this.discoveredSpecies, this.discoveredMutations, this.researchedMutations);
	}

	// Used to sync additional data to the client when breeding statistics change
	protected void writeUpdateData(CompoundTag nbt) {
	}

	@OverridingMethodsMustInvokeSuper
	@Override
	public void readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
		readValuesFromNBT(nbt, value -> {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if (id != null) {
                this.discoveredSpecies.add(id);
            }
        }, SPECIES_KEY);
		readValuesFromNBT(nbt, this.discoveredMutations::add, MUTATIONS_KEY);
		readValuesFromNBT(nbt, this.researchedMutations::add, RESEARCHED_KEY);
	}

	// helper method to call the three writeValuesToNbt
	private void writeAllValues(CompoundTag nbt, Collection<ResourceLocation> discoveredSpecies, Collection<String> discoveredMutations, Collection<String> researchedMutations) {
		// Required for network deserialization in PacketGenomeTrackerSync
		nbt.putString(TYPE_KEY, this.typeId.toString());

		writeValuesToNBT(nbt, Iterables.transform(discoveredSpecies, ResourceLocation::toString), SPECIES_KEY);
		writeValuesToNBT(nbt, discoveredMutations, MUTATIONS_KEY);
		writeValuesToNBT(nbt, researchedMutations, RESEARCHED_KEY);
	}

	// helper method to write a list of strings to an NBT list
	private static void writeValuesToNBT(CompoundTag nbt, Iterable<String> values, String key) {
		ListTag nbtList = new ListTag();
		for (String value : values) {
			nbtList.add(StringTag.valueOf(value));
		}
		nbt.put(key, nbtList);
	}

	// helper method to read strings from an NBT list
	private static void readValuesFromNBT(CompoundTag nbt, Consumer<String> values, String key) {
		if (nbt.contains(key)) {
			ListTag nbtList = nbt.getList(key, Tag.TAG_STRING);
			for (Tag stringTag : nbtList) {
				values.accept(stringTag.getAsString());
			}
		}
	}

	// serializes mutations to strings
	private static String getMutationString(IMutation<?> mutation) {
		String species0 = mutation.getFirstParent().id().toString();
		String species1 = mutation.getSecondParent().id().toString();
		String resultSpecies = mutation.getResult().id().toString();
		return String.format(MUTATION_FORMAT, species0, species1, resultSpecies);
	}

	@Override
	public void registerMutation(IMutation<?> mutation) {
		String mutationString = getMutationString(mutation);
		if (!this.discoveredMutations.contains(mutationString)) {
            this.discoveredMutations.add(mutationString);
			setDirty();

			ISpeciesType<?, ?> speciesRoot = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(this.typeId);
			BreedingTrackerEvent.MutationDiscovered event = new BreedingTrackerEvent.MutationDiscovered(speciesRoot, this.username, mutation, this);
			NeoForge.EVENT_BUS.post(event);

			sendUpdate(List.of(), List.of(mutationString), List.of());
		}
	}

	@Override
	public boolean isDiscovered(IMutation<?> mutation) {
		String mutationString = getMutationString(mutation);
		return this.discoveredMutations.contains(mutationString) || this.researchedMutations.contains(mutationString);
	}

	@Override
	public boolean isDiscovered(ISpecies<?> species) {
		return this.discoveredSpecies.contains(species.id());
	}

	@Override
	public Set<ResourceLocation> getDiscoveredSpecies() {
		return this.discoveredSpecies;
	}

	@Override
	public int getSpeciesBred() {
		return this.discoveredSpecies.size();
	}

	@Override
	public void registerBirth(ISpecies<?> species) {
		registerSpecies(species);
	}

	@Override
	public void registerSpecies(ISpecies<?> species) {
		ResourceLocation speciesId = species.id();

		if (!this.discoveredSpecies.contains(speciesId)) {
            this.discoveredSpecies.add(speciesId);

			ISpeciesType<?, ?> speciesType = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(this.typeId);
			BreedingTrackerEvent.SpeciesDiscovered event = new BreedingTrackerEvent.SpeciesDiscovered(speciesType, this.username, species, this);
			NeoForge.EVENT_BUS.post(event);

			sendUpdate(List.of(speciesId), List.of(), List.of());
		}
	}

	@Override
	public void researchMutation(IMutation<?> mutation) {
		String mutationString = getMutationString(mutation);
		if (!this.researchedMutations.contains(mutationString)) {
            this.researchedMutations.add(mutationString);
			setDirty();

			registerMutation(mutation);

			sendUpdate(List.of(), List.of(), List.of(mutationString));
		}
	}

	@Override
	public boolean isResearched(IMutation<?> mutation) {
		String mutationString = getMutationString(mutation);
		return this.researchedMutations.contains(mutationString);
	}
}
