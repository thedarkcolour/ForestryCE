package forestry.core.genetics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.authlib.GameProfile;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationManager;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.plugin.ISpeciesTypeBuilder;

import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;

public abstract class SpeciesType<S extends ISpecies<I>, I extends IIndividual> implements ISpeciesType<S, I> {
	protected final ResourceLocation id;
	protected final IKaryotype karyotype;
	private final ILifeStage defaultStage;
	private final ImmutableMap<Item, ILifeStage> stages;
	protected final Reference2FloatOpenHashMap<Item> researchMaterials;

	// Initialized in onSpeciesRegistered
	private int speciesCount = -1;
	@Nullable
	private ImmutableMap<ResourceLocation, S> allSpecies;
	@Nullable
	protected IMutationManager<S> mutations;

	public SpeciesType(ResourceLocation id, IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		this.id = id;
		this.karyotype = karyotype;
		this.defaultStage = builder.getDefaultStage();

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
	@SuppressWarnings("unchecked")
	public S getDefaultSpecies() {
		return (S) this.karyotype.getDefaultAllele(this.karyotype.getSpeciesChromosome()).value();
	}

	@Override
	public ILifeStage getDefaultStage() {
		return this.defaultStage;
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
	public void onSpeciesRegistered(ImmutableMap<ResourceLocation, S> allSpecies, IMutationManager<S> mutations) {
		this.speciesCount = allSpecies.size();

		// Note for subclasses: you must call this super method or set the allSpecies yourself. same goes for mutations
		this.allSpecies = allSpecies;
		this.mutations = mutations;
	}

	@Override
	public IMutationManager<S> getMutations() {
		var manager = this.mutations;
		if (manager == null) {
			throw new IllegalStateException("Mutations have not been registered yet.");
		}
		return manager;
	}

	@Override
	public List<S> getAllSpecies() {
		checkSpecies();

		return this.allSpecies.values().asList();
	}

	@Override
	public S getSpecies(ResourceLocation id) {
		checkSpecies();

		S species = this.allSpecies.get(id);
		if (species == null) {
			throw new RuntimeException("No species was found with that ID: " + id);
		}
		return species;
	}

	@Override
	public S getSpeciesSafe(ResourceLocation id) {
		checkSpecies();

		return this.allSpecies.get(id);
	}

	@Override
	public S getRandomSpecies(RandomSource rand) {
		List<S> species = getAllSpecies();
		return species.get(rand.nextInt(species.size()));
	}

	@Override
	public ImmutableSet<ResourceLocation> getAllSpeciesIds() {
		checkSpecies();

		return this.allSpecies.keySet();
	}

	@Override
	public int getSpeciesCount() {
		checkSpecies();

		return this.speciesCount;
	}

	private void checkSpecies() {
		if (this.allSpecies == null) {
			throw new IllegalStateException("Not all species have been registered for type: " + this.id);
		}
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
		tracker.readFromNbt(nbt);
		return tracker;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.id + ']';
	}
}
