/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.genetics;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.LightPreference;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.climate.IClimateManager;
import forestry.api.core.ForestryError;
import forestry.api.core.HumidityType;
import forestry.api.core.IError;
import forestry.api.core.IProduct;
import forestry.api.core.Product;
import forestry.api.core.TemperatureType;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.IIntegerChromosome;
import forestry.api.genetics.pollen.IPollen;
import forestry.api.genetics.pollen.IPollenManager;
import forestry.api.genetics.pollen.IPollenType;
import forestry.core.config.Constants;
import forestry.core.config.ForestryConfig;
import forestry.core.genetics.IndividualLiving;
import forestry.core.genetics.mutations.Mutation;
import forestry.core.utils.SpeciesUtil;
import forestry.core.utils.VecUtil;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

public class Bee extends IndividualLiving<IBeeSpecies, IBee, IBeeSpeciesType> implements IBee {
	public static final Codec<Bee> CODEC = RecordCodecBuilder.create(instance -> {
		Codec<IGenome> genomeCodec = SpeciesUtil.BEE_TYPE.get().getKaryotype().getGenomeCodec();

		return IndividualLiving.livingFields(instance, genomeCodec).and(instance.group(
				Codec.BOOL.fieldOf("pristine").forGetter(IBee::isPristine),
				Codec.INT.optionalFieldOf("generation", 0).forGetter(IBee::getGeneration)
		)).apply(instance, Bee::new);
	});

	private boolean pristine = true;
	private int generation;

	public Bee(IGenome genome) {
		super(genome);
	}

	// For codec
	private Bee(IGenome genome, Optional<IGenome> mate, boolean analyzed, int health, int maxHealth, boolean pristine, int generation) {
		super(genome, mate, analyzed, health, maxHealth);

		this.pristine = pristine;
		this.generation = generation;
	}

	@Override
	protected IIntegerChromosome getLifespanChromosome() {
		return BeeChromosomes.LIFESPAN;
	}

	@Override
	public void setPristine(boolean pristine) {
		this.pristine = pristine;
	}

	@Override
	public boolean isPristine() {
		return this.pristine;
	}

	@Override
	public int getGeneration() {
		return generation;
	}

	/* EFFECTS */
	@Override
	public IEffectData[] doEffect(IEffectData[] storedData, IBeeHousing housing) {
		IBeeEffect effect = genome.getActiveValue(BeeChromosomes.EFFECT);

		storedData[0] = doEffect(effect, storedData[0], housing);

		// Return here if the primary can already not be combined
		if (!effect.isCombinable()) {
			return storedData;
		}

		IBeeEffect secondary = genome.getInactiveValue(BeeChromosomes.EFFECT);
		if (!secondary.isCombinable()) {
			return storedData;
		}

		storedData[1] = doEffect(secondary, storedData[1], housing);

		return storedData;
	}

	private IEffectData doEffect(IBeeEffect effect, IEffectData storedData, IBeeHousing housing) {
		storedData = effect.validateStorage(storedData);
		return effect.doEffect(genome, storedData, housing);
	}

	@Override
	public IEffectData[] doFX(IEffectData[] storedData, IBeeHousing housing) {
		IBeeEffect effect = genome.getActiveValue(BeeChromosomes.EFFECT);

		storedData[0] = doFX(effect, storedData[0], housing);

		// Return here if the primary can already not be combined
		if (!effect.isCombinable()) {
			return storedData;
		}

		IBeeEffect secondary = genome.getInactiveValue(BeeChromosomes.EFFECT);
		if (!secondary.isCombinable()) {
			return storedData;
		}

		storedData[1] = doFX(secondary, storedData[1], housing);

		return storedData;
	}

	private IEffectData doFX(IBeeEffect effect, IEffectData storedData, IBeeHousing housing) {
		return effect.doFX(genome, storedData, housing);
	}

	@Override
	public Set<IError> getCanWork(IBeeHousing housing) {
		Level level = housing.getWorldObj();
		Set<IError> errorStates = new HashSet<>();
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);

		// / Rain needs tolerant flyers
		if (housing.isRaining() && !canFlyInRain(beeModifier)) {
			errorStates.add(ForestryError.IS_RAINING);
		}

		// / Night or darkness requires nocturnal species
		IActivityType type = this.genome.getActiveValue(BeeChromosomes.ACTIVITY);

		if (!beeModifier.isAlwaysActive(genome)) {
			long gameTime = level.getGameTime();
			long dayTime = level.getDayTime();
			BlockPos pos = housing.getCoordinates();

			if (!type.isActive(gameTime, dayTime, pos)) {
				errorStates.add(type.getInactiveError(gameTime, dayTime, pos));
			}

			if (housing.getBlockLightValue() > Constants.APIARY_MIN_LEVEL_LIGHT) {
				if (type.getLightPreference() == LightPreference.DARK) {
					errorStates.add(ForestryError.NOT_GLOOMY);
				}
			} else {
				if (type.getLightPreference() == LightPreference.LIGHT) {
					errorStates.add(ForestryError.NOT_BRIGHT);
				}
			}
		}

		// Check for the sky, except if in hell
		// Whats the chance that a mod dimension will set its sky light to 0 rather than make it always night?
		// I dont think its worth the extra processing to check every block above rather than rely on the cached value
		if (!level.dimensionType().hasCeiling() && !(level.dimension() == Level.END)) {
			if (!housing.canBlockSeeTheSky() && !canWorkUnderground(beeModifier)) {
				errorStates.add(ForestryError.NO_SKY);
			}
		}

		// And finally climate check
		IBeeSpecies species = this.species;

		TemperatureType actualTemperature = housing.temperature();
		TemperatureType beeBaseTemperature = species.getTemperature();
		ToleranceType beeToleranceTemperature = genome.getActiveValue(BeeChromosomes.TEMPERATURE_TOLERANCE);

		if (!ClimateHelper.isWithinLimits(actualTemperature, beeBaseTemperature, beeToleranceTemperature)) {
			if (beeBaseTemperature.ordinal() > actualTemperature.ordinal()) {
				errorStates.add(ForestryError.TOO_COLD);
			} else {
				errorStates.add(ForestryError.TOO_HOT);
			}
		}

		HumidityType actualHumidity = housing.humidity();
		HumidityType beeBaseHumidity = species.getHumidity();
		ToleranceType beeToleranceHumidity = genome.getActiveValue(BeeChromosomes.HUMIDITY_TOLERANCE);

		if (!ClimateHelper.isWithinLimits(actualHumidity, beeBaseHumidity, beeToleranceHumidity)) {
			if (beeBaseHumidity.ordinal() > actualHumidity.ordinal()) {
				errorStates.add(ForestryError.TOO_ARID);
			} else {
				errorStates.add(ForestryError.TOO_HUMID);
			}
		}


		return errorStates;
	}

	private boolean canWorkUnderground(IBeeModifier beeModifier) {
		return genome.getActiveValue(BeeChromosomes.CAVE_DWELLING) || beeModifier.isSunlightSimulated();
	}

	private boolean canFlyInRain(IBeeModifier beeModifier) {
		return genome.getActiveValue(BeeChromosomes.TOLERATES_RAIN) || beeModifier.isSealed();
	}

	private boolean isSuitableBiome(Holder<Biome> biome) {
		IClimateManager manager = IForestryApi.INSTANCE.getClimateManager();
		TemperatureType temperature = manager.getTemperature(biome);
		HumidityType humidity = manager.getHumidity(biome);
		return isSuitableClimate(temperature, humidity);
	}

	private boolean isSuitableClimate(TemperatureType temperature, HumidityType humidity) {
		return ClimateHelper.isWithinLimits(temperature, humidity,
				this.genome.getActiveValue(BeeChromosomes.SPECIES).getTemperature(), this.genome.getActiveValue(BeeChromosomes.TEMPERATURE_TOLERANCE),
				this.genome.getActiveValue(BeeChromosomes.SPECIES).getHumidity(), this.genome.getActiveValue(BeeChromosomes.HUMIDITY_TOLERANCE)
		);
	}

	// todo this can be optimized in a cache somewhere
	@Override
	public List<Holder.Reference<Biome>> getSuitableBiomes(Registry<Biome> registry) {
		return registry.holders().filter(this::isSuitableBiome).toList();
	}
/*
	@Override
	public void age(Level world, float housingLifespanModifier) {
		IBeekeepingMode mode = BeeManager.beeRoot.getBeekeepingMode(world);
		IBeeModifier beeModifier = mode.getBeeModifier();
		float finalModifier = housingLifespanModifier * beeModifier.modifyAging(genome, mate, housingLifespanModifier);

		super.age(world, finalModifier);
	}*/

	// / PRODUCTION
	@Override
	public List<ItemStack> getProduceList() {
		IBeeSpecies primary = this.genome.getActiveValue(BeeChromosomes.SPECIES);
		IBeeSpecies secondary = this.genome.getInactiveValue(BeeChromosomes.SPECIES);

		if (primary == secondary) {
			List<IProduct> products = primary.getProducts();
			ArrayList<ItemStack> stacks = new ArrayList<>(products.size());

			for (var product : products) {
				stacks.add(product.createStack());
			}
			return stacks;
		} else {
			// No duplicates
			ObjectOpenCustomHashSet<IProduct> products = new ObjectOpenCustomHashSet<>(primary.getProducts().size(), Product.ITEM_ONLY_STRATEGY);
			ArrayList<ItemStack> stacks = new ArrayList<>(products.size() + secondary.getProducts().size());

			for (var product : primary.getProducts()) {
				products.add(product);
				stacks.add(product.createStack());
			}
			for (var product : secondary.getProducts()) {
				if (!products.contains(product)) {
					stacks.add(product.createStack());
				}
			}

			return stacks;
		}
	}

	@Override
	public List<ItemStack> getSpecialtyList() {
		List<IProduct> products = this.genome.getActiveValue(BeeChromosomes.SPECIES).getSpecialties();
		ArrayList<ItemStack> stacks = new ArrayList<>(products.size());

		for (var product : products) {
			stacks.add(product.createStack());
		}

		return stacks;
	}

	@Override
	public List<ItemStack> produceStacks(IBeeHousing housing) {
		Level level = housing.getWorldObj();
		//IBeekeepingMode mode = BeeManager.beeRoot.getBeekeepingMode(world);

		ArrayList<ItemStack> stacks = new ArrayList<>();

		IBeeSpecies primary = this.species;
		IBeeSpecies secondary = this.inactiveSpecies;

		IBeeModifier beeHousingModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
		//IBeeModifier beeModeModifier = mode.getBeeModifier();

		// Bee genetic speed * beehousing * beekeeping mode
		float speed = beeHousingModifier.modifyProductionSpeed(genome, genome.getActiveValue(BeeChromosomes.SPEED))/* * beeModeModifier.modifyProductionSpeed(genome, 1f)*/;
		RandomSource rand = level.random;

		// / Primary Products
		for (var product : primary.getProducts()) {
			if (rand.nextFloat() < product.chance() * speed) {
				stacks.add(product.createRandomStack(rand));
			}
		}
		// / Secondary Products
		for (var product : secondary.getProducts()) {
			if (rand.nextFloat() < Math.round(product.chance() / 2f) * speed) {
				stacks.add(product.createRandomStack(rand));
			}
		}

		// / Specialty products
		if (primary.isJubilant(genome, housing) && secondary.isJubilant(genome, housing)) {
			for (var product : primary.getSpecialties()) {
				if (rand.nextFloat() < product.chance() * speed) {
					stacks.add(product.createRandomStack(rand));
				}
			}
		}

		BlockPos housingCoordinates = housing.getCoordinates();
		return genome.getActiveValue(BeeChromosomes.FLOWER_TYPE).affectProducts(level, housingCoordinates, this, stacks);
	}

	/* REPRODUCTION */
	@Override
	@Nullable
	public IBee spawnPrincess(IBeeHousing housing) {
		// We need a mated queen to produce offspring.
		if (this.mate == null) {
			return null;
		}

		// Fatigued (dead ignoble) queens do not produce princesses.
		if (!this.pristine) {
			IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
			RandomSource rand = housing.getWorldObj().random;

			if (checkIgnobleDecay(rand, this.generation, beeModifier.modifyGeneticDecay(this.genome, 1f))) {
				return null;
			}
		}

		return createOffspring(housing, mate, getGeneration() + 1);
	}

	public static boolean checkIgnobleDecay(RandomSource rand, int generation, float modifier) {
		return (generation > 96 + rand.nextInt(6) + rand.nextInt(6)) && (rand.nextFloat() < 0.02f * modifier);
	}

	@Override
	public List<IBee> spawnDrones(IBeeHousing housing) {
		// We need a mated queen to produce offspring.
		if (mate == null) {
			return Collections.emptyList();
		}

		List<IBee> bees = new ArrayList<>();
		//Level level = housing.getWorldObj();
		//BlockPos housingPos = housing.getCoordinates();

		int toCreate = this.genome.getActiveValue(BeeChromosomes.FERTILITY);//BeeManager.beeRoot.getBeekeepingMode(level).getFinalFertility(this, level, housingPos);

		if (toCreate <= 0) {
			toCreate = 1;
		}

		for (int i = 0; i < toCreate; i++) {
			IBee offspring = createOffspring(housing, mate, 0);
			offspring.setPristine(true);
			bees.add(offspring);
		}

		return bees;
	}

	private IBee createOffspring(IBeeHousing housing, IGenome mate, int generation) {
		SpeciesUtil.ISpeciesMutator mutator = (p1, p2) -> mutateSpecies(housing, p1, p2);
		// drones are always generation 0
		boolean haploid = generation == 0 && ForestryConfig.SERVER.useHaploidDrones.get();

		return SpeciesUtil.createOffspring(housing.getWorldObj().random, this.genome, mate, mutator, genome -> {
			//IBeekeepingMode mode = BeeManager.beeRoot.getBeekeepingMode(level);
			int maxHealth = genome.getActiveValue(BeeChromosomes.LIFESPAN);
			return new Bee(genome, Optional.empty(), false, maxHealth, maxHealth, this.pristine, generation); /*mode.isOffspringPristine(this)*/
		}, haploid);
	}

	@Nullable
	@SuppressWarnings("CodeBlock2Expr")
	private static ImmutableList<AllelePair<?>> mutateSpecies(IBeeHousing housing, IGenome parent1, IGenome parent2) {
		return SpeciesUtil.mutateSpecies(housing.getWorldObj(), housing.getCoordinates(), housing.getOwner(), parent1, parent2, BeeChromosomes.SPECIES, (mutation, level, pos, firstGenome, secondGenome, climate) -> {
			return getChance(mutation, housing, firstGenome, secondGenome);
		});
	}

	private static float getChance(IMutation<IBeeSpecies> mutation, IBeeHousing housing, IGenome genome0, IGenome genome1) {
		Level level = housing.getWorldObj();
		BlockPos housingPos = housing.getCoordinates();

		float currentChance = Mutation.getChance(mutation, level, housingPos, genome0, genome1, housing);
		if (currentChance <= 0) {
			return 0;
		}

		IBeeModifier beeHousingModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
		//IBeeModifier beeModeModifier = BeeManager.beeRoot.getBeekeepingMode(world).getBeeModifier();

		currentChance = beeHousingModifier.modifyMutationChance(genome0, genome1, mutation, currentChance);
		//currentChance *= beeModeModifier.modifyMutationChance(genome0, genome1, currentChance);

		return currentChance;
	}

	/* FLOWERS */
	@Nullable
	@Override
	public IPollen<?> retrievePollen(IBeeHousing housing) {
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);

		int chance = getAdjustedPollination(this.genome, beeModifier);

		Level level = housing.getWorldObj();
		RandomSource random = level.random;

		if (random.nextInt(100) >= chance) {
			return null;
		}

		Vec3i area = getAdjustedTerritory(genome, beeModifier);
		Vec3i offset = new Vec3i(-area.getX() / 2, -area.getY() / 4, -area.getZ() / 2);
		BlockPos housingPos = housing.getCoordinates();
		IPollenManager pollens = IForestryApi.INSTANCE.getPollenManager();

		for (int i = 0; i < 20; i++) {
			BlockPos randomPos = VecUtil.sum(housingPos, VecUtil.getRandomPositionInArea(random, area), offset);

			if (level.hasChunkAt(randomPos)) {
				IPollen<?> pollen = pollens.getPollen(level, randomPos, this);

				if (pollen != null) {
					return pollen;
				}
			}
		}

		return null;
	}

	@Override
	public boolean pollinateRandom(IBeeHousing housing, IPollen<?> pollen) {
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);

		int chance = getAdjustedPollination(genome, beeModifier);

		Level level = housing.getWorldObj();
		RandomSource random = level.random;

		// Correct speed
		if (random.nextInt(100) >= chance) {
			return false;
		}

		Vec3i area = getAdjustedTerritory(genome, beeModifier);
		Vec3i offset = new Vec3i(-area.getX() / 2, -area.getY() / 4, -area.getZ() / 2);
		BlockPos housingPos = housing.getCoordinates();
		IPollenType<?> type = pollen.getType();

		for (int i = 0; i < 30; i++) {
			BlockPos randomPos = VecUtil.sum(housingPos, VecUtil.getRandomPositionInArea(random, area), offset);

			if (type.tryPollinate(level, randomPos, pollen.castPollen(), this)) {
				// for debugging purposes
				//Forestry.LOGGER.debug("Hive {} pollinated at {}", housingPos, randomPos);
				return true;
			}
		}

		return false;
	}

	@Nullable
	@Override
	public BlockPos plantFlowerRandom(IBeeHousing housing, List<BlockState> potentialFlowers) {
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);

		int chance = getAdjustedPollination(this.genome, beeModifier);

		Level level = housing.getWorldObj();
		RandomSource random = level.random;

		// Correct speed
		if (random.nextInt(100) >= chance) {
			return null;
		}
		// Gather required info
		IFlowerType flowerType = genome.getActiveValue(BeeChromosomes.FLOWER_TYPE);
		Vec3i area = getAdjustedTerritory(genome, beeModifier);
		Vec3i offset = new Vec3i(-area.getX() / 2, -area.getY() / 4, -area.getZ() / 2);
		BlockPos housingPos = housing.getCoordinates();

		for (int i = 0; i < 10; i++) {
			BlockPos randomPos = VecUtil.getRandomPositionInArea(random, area);
			BlockPos posBlock = VecUtil.sum(housingPos, randomPos, offset);

			if (flowerType.plantRandomFlower(level, posBlock, potentialFlowers)) {
				return posBlock;
			}
		}
		return null;
	}

	@Override
	public Iterator<BlockPos.MutableBlockPos> getAreaIterator(IBeeHousing housing) {
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
		Vec3i area = getAdjustedTerritory(this.genome, beeModifier);
		BlockPos housingPos = housing.getCoordinates();
		BlockPos minPos = housingPos.offset(-area.getX() / 2, -area.getY() / 2, -area.getZ() / 2);
		BlockPos maxPos = minPos.offset(area);
		Level level = housing.getWorldObj();
		return VecUtil.getAllInBoxFromCenterMutable(level, minPos, housingPos, maxPos);
	}

	public static Vec3i getParticleArea(IGenome genome, IBeeHousing housing) {
		Vec3i area = getAdjustedTerritory(genome, IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing));
		int x = area.getX();
		int y = area.getY();
		int z = area.getZ();

		if (x < 1) {
			x = 1;
		}
		if (y < 1) {
			y = 1;
		}
		if (z < 1) {
			z = 1;
		}

		return new Vec3i(x, y, z);
	}

	// todo reimplement beekeeping mode through config
	public static Vec3i getAdjustedTerritory(IGenome genome, IBeeModifier beeModifier) {
		return beeModifier.modifyTerritory(genome, genome.getActiveValue(BeeChromosomes.TERRITORY));
	}

	private static int getAdjustedPollination(IGenome genome, IBeeModifier beeModifier) {
		return Math.round(beeModifier.modifyPollination(genome, genome.getActiveValue(BeeChromosomes.POLLINATION)));
	}

	@Override
	protected void copyPropertiesTo(IBee other) {
		super.copyPropertiesTo(other);

		Bee bee = (Bee) other;
		bee.pristine = this.pristine;
		bee.generation = this.generation;
	}
}
