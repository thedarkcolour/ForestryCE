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
package forestry.lepidopterology.genetics;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import forestry.api.IForestryApi;
import forestry.api.core.HumidityType;
import forestry.api.core.IProduct;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.alleles.IIntegerChromosome;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.IEntityButterfly;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.core.genetics.IndividualLiving;
import forestry.core.genetics.mutations.Mutation;
import forestry.core.utils.SpeciesUtil;
import forestry.lepidopterology.ModuleLepidopterology;

public class Butterfly extends IndividualLiving<IButterflySpecies, IButterfly, IButterflySpeciesType> implements IButterfly {
	private static final RandomSource rand = RandomSource.create();

	public static final Codec<Butterfly> CODEC = RecordCodecBuilder.create(instance -> {
		Codec<IGenome> genomeCodec = SpeciesUtil.BUTTERFLY_TYPE.get().getKaryotype().getGenomeCodec();

		return IndividualLiving.livingFields(instance, genomeCodec).apply(instance, Butterfly::new);
	});

	public Butterfly(IGenome genome) {
		super(genome);
	}

	// For codec
	private Butterfly(IGenome genome, Optional<IGenome> mate, boolean analyzed, int health, int maxHealth) {
		super(genome, mate, analyzed, health, maxHealth);
	}

	@Override
	public Component getDisplayName() {
		return this.species.getDisplayName();
	}

	@Override
	public boolean canSpawn(Level level, double x, double y, double z) {
		if (!canFly(level)) {
			return false;
		}

		BlockPos pos = BlockPos.containing(x, y, z);
		Holder<Biome> biome = level.getBiome(pos);
		IButterflySpecies species = getGenome().getActiveValue(ButterflyChromosomes.SPECIES);
		return (species.getSpawnBiomes() == null || biome.is(species.getSpawnBiomes())) && isAcceptedEnvironment(level, pos);
	}

	@Override
	public boolean canTakeFlight(Level level, double x, double y, double z) {
		return canFly(level) && isAcceptedEnvironment(level, x, y, z);
	}

	private boolean canFly(Level world) {
		return (!world.isRaining() || getGenome().getActiveValue(ButterflyChromosomes.TOLERATES_RAIN)) && isActiveThisTime(world.isDay());
	}

	@Override
	public boolean isAcceptedEnvironment(Level world, BlockPos pos) {
		Holder<Biome> biome = world.getBiome(pos);
		TemperatureType biomeTemperature = IForestryApi.INSTANCE.getClimateManager().getTemperature(biome);
		HumidityType biomeHumidity = IForestryApi.INSTANCE.getClimateManager().getHumidity(biome);
		return ClimateHelper.isWithinLimits(biomeTemperature, biomeHumidity,
				getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getTemperature(), getGenome().getActiveValue(ButterflyChromosomes.TEMPERATURE_TOLERANCE),
				getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getHumidity(), getGenome().getActiveValue(ButterflyChromosomes.HUMIDITY_TOLERANCE));
	}

	@Nullable
	@Override
	public IButterfly spawnCaterpillar(IButterflyNursery nursery) {
		// We need a mated queen to produce offspring.
		if (this.mate == null) {
			return null;
		}

		SpeciesUtil.ISpeciesMutator mutator = (p1, p2) -> mutateSpecies(nursery, p1, p2);
		return SpeciesUtil.createOffspring(nursery.getWorldObj().random, this.genome, this.mate, mutator, Butterfly::new);
	}

	@Nullable
	private static ImmutableList<AllelePair<?>> mutateSpecies(IButterflyNursery nursery, IGenome parent1, IGenome parent2) {
		return SpeciesUtil.mutateSpecies(nursery.getWorldObj(), nursery.getCoordinates(), null, parent1, parent2, ButterflyChromosomes.SPECIES, Mutation::getChance);
	}

	private boolean isActiveThisTime(boolean isDayTime) {
		if (getGenome().getActiveValue(ButterflyChromosomes.NEVER_SLEEPS)) {
			return true;
		}

		return isDayTime != getGenome().getActiveValue(ButterflyChromosomes.SPECIES).isNocturnal();
	}

	@Override
	protected IIntegerChromosome getLifespanChromosome() {
		return ButterflyChromosomes.LIFESPAN;
	}

	@Override
	public List<ItemStack> getLootDrop(IEntityButterfly entity, boolean playerKill, int lootLevel) {
		ArrayList<ItemStack> drop = new ArrayList<>();

		PathfinderMob creature = entity.getEntity();
		float metabolism = (float) getGenome().getActiveValue(ButterflyChromosomes.METABOLISM) / 10;
		List<IProduct> products = species.getButterflyLoot();
		RandomSource rand = creature.level().random;

		for (IProduct product : products) {
			if (rand.nextFloat() < product.chance() * metabolism) {
				drop.add(product.createRandomStack(rand));
			}
		}

		return drop;
	}

	@Override
	public List<ItemStack> getCaterpillarDrop(IButterflyNursery nursery, boolean playerKill, int lootLevel) {
		ArrayList<ItemStack> drop = new ArrayList<>();
		float metabolism = (float) getGenome().getActiveValue(ButterflyChromosomes.METABOLISM) / 10;
		List<IProduct> products = getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getCaterpillarProducts();

		for (IProduct product : products) {
			if (rand.nextFloat() < product.chance() * metabolism) {
				drop.add(product.createRandomStack(rand));
			}
		}

		return drop;
	}

	@Override
	public List<ItemStack> getCocoonDrop(boolean includeButterfly, IButterflyCocoon cocoon) {
		ArrayList<ItemStack> drop = new ArrayList<>();
		float metabolism = (float) getGenome().getActiveValue(ButterflyChromosomes.METABOLISM) / 10;
		List<IProduct> products = cocoon.getProducts();

		for (IProduct product : products) {
			if (rand.nextFloat() < product.chance() * metabolism) {
				drop.add(product.createRandomStack(rand));
			}
		}

		IButterflySpeciesType butterflyType = SpeciesUtil.BUTTERFLY_TYPE.get();

		if (ModuleLepidopterology.getSerumChance() > 0) {
			if (rand.nextFloat() < ModuleLepidopterology.getSerumChance() * metabolism) {
				ItemStack stack = butterflyType.createStack(this, ButterflyLifeStage.SERUM);
				if (ModuleLepidopterology.getSecondSerumChance() > 0) {
					if (rand.nextFloat() < ModuleLepidopterology.getSecondSerumChance() * metabolism) {
						stack.setCount(2);
					}
				}
				drop.add(butterflyType.createStack(this, ButterflyLifeStage.SERUM));
			}
		}

		if (includeButterfly) {
			drop.add(butterflyType.createStack(this, ButterflyLifeStage.BUTTERFLY));
		}

		return drop;
	}
}
