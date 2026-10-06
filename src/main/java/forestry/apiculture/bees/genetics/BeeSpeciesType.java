package forestry.apiculture.bees.genetics;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import forestry.api.IForestryApi;
import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.core.genetics.*;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.apiimpl.ForestryApiImpl;
import forestry.apiculture.plugin.ApicultureRegistration;
import forestry.core.platform.config.ForestryConfig;
import forestry.core.engine.genetics.BreedingTracker;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.engine.genetics.root.BreedingTrackerManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BeeSpeciesType extends SpeciesType<IBeeSpecies, IBee> implements IBeeSpeciesType {
	// Reference-value registries backing the flower_type, bee_effect, activity, and jubilance chromosomes.
	private ImmutableMap<ResourceLocation, IBeeEffect> beeEffects = ImmutableMap.of();
	private ImmutableMap<ResourceLocation, IBeeEffect> codeEffects = ImmutableMap.of();
	@Nullable
	private ImmutableMap<ResourceLocation, IActivityType> activityTypes;
	@Nullable
	private ImmutableMap<ResourceLocation, IBeeJubilance> jubilances;
	private ImmutableMap<ResourceLocation, IBeeJubilance> codeJubilances = ImmutableMap.of();

	public BeeSpeciesType(IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		super(ForestrySpeciesTypes.BEE, karyotype, builder);
	}

	@Override
	public IBeeEffect getBeeEffect(ResourceLocation id) {
		return getMapValue(this.beeEffects, id, "bee effect");
	}

	public void setBeeEffects(ImmutableMap<ResourceLocation, IBeeEffect> beeEffects) {
		this.beeEffects = beeEffects;
	}

	public ImmutableMap<ResourceLocation, IBeeEffect> getCodeBeeEffects() {
		return this.codeEffects;
	}

	@Override
	public IActivityType getActivityType(ResourceLocation id) {
		return getMapValue(this.activityTypes, id, "activity type");
	}

	@Override
	public IBeeJubilance getJubilance(ResourceLocation id) {
		return getMapValue(this.jubilances, id, "bee jubilance");
	}

	@Nullable
	@Override
	public IBeeJubilance getJubilanceSafe(ResourceLocation id) {
		return getMapValueSafe(this.jubilances, id);
	}

	public void setJubilances(ImmutableMap<ResourceLocation, IBeeJubilance> jubilances) {
		this.jubilances = jubilances;
	}

	public ImmutableMap<ResourceLocation, IBeeJubilance> getCodeJubilances() {
		return this.codeJubilances;
	}

	@Override
	public ILifeStage getTypeForMutation(int position) {
		return switch (position) {
			case 0 -> BeeLifeStage.PRINCESS;
			case 1 -> BeeLifeStage.DRONE;
			case 2 -> BeeLifeStage.QUEEN;
			default -> getDefaultStage();
		};
	}

	@Override
	public boolean isDrone(ItemStack stack) {
		return getLifeStage(stack) == BeeLifeStage.DRONE;
	}

	@Override
	public boolean isMated(ItemStack stack) {
		return IIndividualHandlerItem.filter(stack, (individual, stage) -> {
			return stage == BeeLifeStage.QUEEN && individual.getMate() != null;
		});
	}

	@Override
	public IApiaristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile) {
		return BreedingTrackerManager.INSTANCE.getTracker(this, level, profile);
	}

	@Override
	public String getBreedingTrackerFile(@Nullable GameProfile profile) {
		return "ApiaristTracker." + (profile == null ? "common" : profile.getId());
	}

	@Override
	public IBreedingTracker createBreedingTracker() {
		return new ApiaristTracker();
	}

	@Override
	public void initializeBreedingTracker(IBreedingTracker tracker, @Nullable Level world, @Nullable GameProfile profile) {
		if (tracker instanceof BreedingTracker apiaristTracker) {
			apiaristTracker.setLevel(world);
			apiaristTracker.setUsername(profile);
		}
	}

	@Override
	public boolean isMember(IIndividual individual) {
		return individual instanceof IBee;
	}

	@Override
	public Codec<? extends IBee> getIndividualCodec() {
		return Bee.CODEC;
	}

	@Override
	public float getResearchSuitability(IBeeSpecies species, ItemStack stack) {
		for (IProduct product : species.getProducts()) {
			if (stack.is(product.item())) {
				return 1.0f;
			}
		}
		for (IProduct product : species.getSpecialties()) {
			if (stack.is(product.item())) {
				return 1.0f;
			}
		}
		return super.getResearchSuitability(species, stack);
	}

	@Override
	public List<ItemStack> getResearchBounty(IBeeSpecies species, Level level, GameProfile researcher, IBee individual, int bountyLevel) {
		List<ItemStack> bounty = super.getResearchBounty(species, level, researcher, individual, bountyLevel);
		if (bountyLevel > 10) {
			for (IProduct stack : species.getSpecialties()) {
				bounty.add(formBountyStack(stack, bountyLevel, level.random));
			}
		}
		for (IProduct stack : species.getProducts()) {
			bounty.add(formBountyStack(stack, bountyLevel, level.random));
		}
		return bounty;
	}


	@Override
	public ImmutableMap<ResourceLocation, IBeeSpecies> handleSpeciesRegistration(List<IForestryPlugin> plugins) {
		ApicultureRegistration registration = new ApicultureRegistration(this);

		for (IForestryPlugin plugin : plugins) {
			plugin.registerApiculture(registration);
		}

		// store the reference-value registries backing the flower_type, bee_effect, and activity chromosomes
		this.codeEffects = registration.getBeeEffects();
		this.beeEffects = this.codeEffects; // bootstrap: code base alone until the first datapack load
		this.activityTypes = registration.getActivityTypes();
		this.codeJubilances = registration.getJubilances();
		this.jubilances = this.codeJubilances;

		// initialize hive manager
		((ForestryApiImpl) IForestryApi.INSTANCE).setHiveManager(registration.buildHiveManager());

		// Bee species are no longer built at setup; they come exclusively from the datapack loader
		// (BeeSpeciesManager -> ApicultureReloadHandler.rebuildSpecies), which replaces this species
		// type's species map on datapack reload.
		return ImmutableMap.of();
	}

	private ItemStack formBountyStack(IProduct product, int bountyLevel, RandomSource rand) {
		double productGenChance = product.chance() * ForestryConfig.SERVER.escritoireBountyMultiplier.get();
		int productGenSuccessCounter = 0;
		for (int i = 0; i < bountyLevel; i++) {
			double randVal = rand.nextDouble();
			if (randVal < productGenChance) {
				productGenSuccessCounter++;
			}
		}
		ItemStack copy = product.createRandomStack(rand);
		int stackMaxSize = copy.getMaxStackSize();
		copy.setCount(Math.min(productGenSuccessCounter, stackMaxSize));
		return copy;
	}

	@Override
	public DataProvider createSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<IApicultureRegistration> species) {
		return new BeeSpeciesProvider(output, registries, this, species);
	}
}
