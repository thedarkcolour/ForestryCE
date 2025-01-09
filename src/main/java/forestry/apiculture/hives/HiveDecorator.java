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
package forestry.apiculture.hives;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import forestry.api.IForestryApi;
import forestry.api.apiculture.hives.IHive;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.core.config.ForestryConfig;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class HiveDecorator extends Feature<NoneFeatureConfiguration> {
	public HiveDecorator() {
		super(NoneFeatureConfiguration.CODEC);
	}

	public static boolean tryGenHive(WorldGenLevel world, RandomSource rand, int posX, int posZ, IHive hive) {
		final BlockPos hivePos = hive.getPosForHive(world, rand, posX, posZ);

		if (hivePos == null) {
			return false;
		}

		//Forestry.LOGGER.debug("Attempting to place hive {} at pos {}", hive, hivePos);

		if (!hive.canReplace(world, hivePos)) {
			//Forestry.LOGGER.debug("Failed to place hive: could not replace block {}", world.getBlockState(hivePos));
			return false;
		}

		Holder<Biome> biome = world.getBiome(hivePos);
		TemperatureType temperature = IForestryApi.INSTANCE.getClimateManager().getTemperature(biome);
		HumidityType humidity = IForestryApi.INSTANCE.getClimateManager().getHumidity(biome);
		//Check if the biome is valid
		if (!hive.isGoodBiome(biome) || !hive.isGoodTemperature(temperature) || !hive.isGoodHumidity(humidity)) {
			return false;
		}

		if (!hive.isValidLocation(world, hivePos)) {
			//Forestry.LOGGER.debug("Failed to place hive: could not place block at {}", hivePos);
			return false;
		}

		return setHive(world, rand, hivePos, hive);
	}

	private static boolean setHive(WorldGenLevel world, RandomSource rand, BlockPos pos, IHive hive) {
		BlockState hiveState = hive.getHiveBlockState();
		Block hiveBlock = hiveState.getBlock();
		boolean placed = world.setBlock(pos, hiveState, Block.UPDATE_CLIENTS);
		if (!placed) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block placedBlock = state.getBlock();
		if (!(hiveBlock == placedBlock)) {
			return false;
		}

		hiveBlock.onPlace(state, world.getLevel(), pos, hiveState, false);

		hive.postGen(world, rand, pos);

		return true;
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource rand = context.random();
		BlockPos pos = context.origin();

		ObjectArrayList<IHive> hives = new ObjectArrayList<>(IForestryApi.INSTANCE.getHiveManager().getHives());
		int numTries = (int) Math.ceil(hives.size() / 2f);
		double baseChance = ForestryConfig.SERVER.wildHiveSpawnRate.get() * hives.size() / 8;
		Util.shuffle(hives, rand);

		for (int tries = 0; tries < numTries; tries++) {
			for (IHive hive : hives) {
				if (hive.genChance() * baseChance >= rand.nextFloat() * 100.0f) {
					int posX = pos.getX() + rand.nextInt(16);
					int posZ = pos.getZ() + rand.nextInt(16);

					if (tryGenHive(level, rand, posX, posZ, hive)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
