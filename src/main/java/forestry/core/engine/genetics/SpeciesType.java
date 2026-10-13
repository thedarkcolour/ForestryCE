package forestry.core.engine.genetics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import forestry.api.core.genetics.*;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.api.plugin.ISpeciesTypeBuilder;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SpeciesType<S extends ISpecies<I>, I extends IIndividual> implements ISpeciesType<S, I> {
	protected final ResourceLocation id;
	protected final IKaryotype karyotype;
	private final ILifeStage defaultStage;
	private final String translationKey;
	private final ImmutableMap<Item, ILifeStage> stages;
	protected final Reference2FloatOpenHashMap<Item> researchMaterials;

	// Empty until species are loaded (datapack on the server, sync packet on the client). Never null. Volatile:
	// swapped by setSpecies from the reload/sync path, read by gameplay/JEI/GUI on many threads.
	private volatile ImmutableMap<ResourceLocation, S> allSpecies = ImmutableMap.of();
	// Empty until the mutation recipes are loaded by the reload handler. Never null. Volatile: rebuilt from the server
	// game executor (AddReloadListenerEvent) and the client thread (RecipesUpdatedEvent), read by gameplay/JEI/GUI.
	private volatile IMutationManager<S> mutations = new MutationManager<>(com.google.common.collect.ImmutableList.of());

	public SpeciesType(ResourceLocation id, IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		this.id = id;
		this.karyotype = karyotype;
		this.defaultStage = builder.getDefaultStage();
		this.translationKey = Util.makeDescriptionId("species_type", id);

		List<ILifeStage> stages = builder.getStages();
		ImmutableMap.Builder<Item, ILifeStage> stagesBuilder = ImmutableMap.builderWithExpectedSize(stages.size());
		for (ILifeStage stage : stages) {
			stagesBuilder.put(stage.getItemForm(), stage);
		}
		this.stages = stagesBuilder.build();

		this.researchMaterials = new Reference2FloatOpenHashMap<>();
		builder.buildResearchMaterials(this.researchMaterials);
	}

	public ResourceLocation id() {
		return this.id;
	}

	@Override
	public S getDefaultSpecies() {
		return getSpecies(this.karyotype.getDefaultSpecies());
	}

	@Override
	public ILifeStage getDefaultStage() {
		return this.defaultStage;
	}

	@Override
	public String getTranslationKey() {
		return this.translationKey;
	}

	@Override
	public Collection<ILifeStage> getLifeStages() {
		return this.stages.values();
	}

	@Nullable
	@Override
	public ILifeStage getLifeStage(ItemStack stack) {
		return this.stages.get(stack.getItem());
	}

	@Override
	public IKaryotype getKaryotype() {
		return this.karyotype;
	}

	@OverridingMethodsMustInvokeSuper
	@Override
	public void onSpeciesRegistered(ImmutableMap<ResourceLocation, S> allSpecies) {
		setSpecies(allSpecies);
	}

	@ApiStatus.Internal
	public void setSpecies(ImmutableMap<ResourceLocation, S> allSpecies) {
		this.allSpecies = allSpecies;
	}

	@ApiStatus.Internal
	public void setMutations(IMutationManager<S> mutations) {
		this.mutations = mutations;
	}

	@Override
	public IMutationManager<S> getMutations() {
		return this.mutations;
	}

	@Override
	public List<S> getAllSpecies() {
		return this.allSpecies.values().asList();
	}

	@Override
	public S getSpecies(ResourceLocation id) {
		S species = this.allSpecies.get(id);
		if (species == null) {
			throw new RuntimeException("No species was found with that ID: " + id);
		}
		return species;
	}

	@Override
	public S getSpeciesSafe(ResourceLocation id) {
		return this.allSpecies.get(id);
	}

	@Override
	public S getRandomSpecies(RandomSource rand) {
		List<S> species = getAllSpecies();
		return species.get(rand.nextInt(species.size()));
	}

	@Override
	public ImmutableSet<ResourceLocation> getAllSpeciesIds() {
		return this.allSpecies.keySet();
	}

	@Override
	public int getSpeciesCount() {
		// Derived from the volatile map rather than a separate counter field, so a reader that observes a freshly
		// swapped species map can never see a stale count from a non-atomic pair of writes.
		return this.allSpecies.size();
	}

	// registry map getter with logging
	protected static <V> V getMapValue(@Nullable ImmutableMap<ResourceLocation, V> map, ResourceLocation id, String what) {
		if (map == null) {
			throw new IllegalStateException(what + " have not been registered yet (looking up " + id + ").");
		}
		V value = map.get(id);
		if (value == null) {
			throw new IllegalArgumentException("No " + what + " was registered with the ID: " + id);
		}
		return value;
	}

	@Nullable
	protected static <V> V getMapValueSafe(@Nullable ImmutableMap<ResourceLocation, V> map, ResourceLocation id) {
		return map == null ? null : map.get(id);
	}

	@Override
	public float getResearchSuitability(S species, ItemStack stack) {
		return this.researchMaterials.getFloat(stack.getItem());
	}

	@Override
	public List<ItemStack> getResearchBounty(S species, Level level, GameProfile researcher, I individual, int bountyLevel) {
		ArrayList<ItemStack> list = new ArrayList<>();

		if (level.random.nextFloat() < bountyLevel / 16f) {
			List<IMutation<S>> mutationsFrom = getMutations().getMutationsFrom(species);

			if (!mutationsFrom.isEmpty()) {
				ArrayList<IMutation<?>> unresearchedMutations = new ArrayList<>();
				IBreedingTracker tracker = getBreedingTracker(level, researcher);

				for (IMutation<?> mutation : mutationsFrom) {
					if (!tracker.isResearched(mutation)) {
						unresearchedMutations.add(mutation);
					}
				}

				IMutation<?> chosenMutation;
				if (!unresearchedMutations.isEmpty()) {
					chosenMutation = unresearchedMutations.get(level.random.nextInt(unresearchedMutations.size()));
				} else {
					chosenMutation = mutationsFrom.get(level.random.nextInt(mutationsFrom.size()));
				}

				ItemStack researchNote = chosenMutation.getMutationNote(researcher);
				list.add(researchNote);
				return list;
			}
		}

		return new ArrayList<>();
	}

	@Override
	public ItemStack createStack(I individual, ILifeStage type) {
		if (!this.stages.containsValue(type)) {
			throw new IllegalArgumentException("Invalid life stage for species type " + this.id + ": " + type);
		}
		return individual.createStack(type);
	}

	@Override
	public ItemStack createStack(ResourceLocation speciesId, ILifeStage stage) {
		S species = getSpecies(speciesId);
		return createStack(species.createIndividual(), stage);
	}

	@Override
	public I createRandomIndividual(RandomSource rand) {
		List<S> allSpecies = getAllSpecies();
		return allSpecies.get(rand.nextInt(allSpecies.size())).createIndividual();
	}

	@Override
	public IBreedingTracker createBreedingTracker(CompoundTag nbt) {
		IBreedingTracker tracker = createBreedingTracker();
		tracker.load(nbt);
		return tracker;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.id + ']';
	}
}
