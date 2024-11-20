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
package forestry.arboriculture.worldgen;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.minecraftforge.common.IPlantable;

import forestry.api.IForestryApi;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.climate.IClimateManager;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.arboriculture.TreeConfig;
import forestry.arboriculture.commands.TreeGenHelper;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.SpeciesUtil;

public class TreeDecorator extends Feature<NoneFeatureConfiguration> {
	private static final IdentityHashMap<ResourceKey<Biome>, List<ITree>> BIOME_CACHE = new IdentityHashMap<>();

	public TreeDecorator() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Nullable
	private static BlockPos getValidPos(WorldGenLevel world, int x, int z, ITree tree) {
		// get to the ground
		final BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
		if (topPos.getY() == 0) {
			return null;
		}

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(topPos.getX(), topPos.getY(), topPos.getZ());

		BlockState blockState = world.getBlockState(pos);
		while (BlockUtil.canReplace(blockState, world, pos)) {
			pos.move(Direction.DOWN);
			if (pos.getY() <= 0) {
				return null;
			}

			blockState = world.getBlockState(pos);
		}

		if (tree instanceof IPlantable plantable && blockState.getBlock().canSustainPlant(blockState, world, pos, Direction.UP, plantable)) {
			return pos.above();
		}

		return null;
	}

	// haven't looked into whether this is redundant but better safe than sorry
	private static synchronized void generateBiomeCache(WorldGenLevel level) {
		if (!BIOME_CACHE.isEmpty()) {
			return;
		}
		List<ITreeSpecies> allSpecies = SpeciesUtil.getAllTreeSpecies();
		IClimateManager manager = IForestryApi.INSTANCE.getClimateManager();
		// correctly dedupe ITree instances with map instead of using set
		IdentityHashMap<ITreeSpecies, ITree> treeInstances = new IdentityHashMap<>(allSpecies.size());

		level.registryAccess().registryOrThrow(Registries.BIOME).holders().forEach(biome -> {
			List<ITree> trees = BIOME_CACHE.computeIfAbsent(biome.key(), k -> new ArrayList<>());
			TemperatureType temperature = manager.getTemperature(biome);
			HumidityType humidity = manager.getHumidity(biome);

			for (ITreeSpecies species : allSpecies) {
				if (species.getRarity() > 0.0f) {
					// todo tolerance chromosomes
					if (temperature == species.getTemperature() && humidity == species.getHumidity()) {
						trees.add(treeInstances.computeIfAbsent(species, k -> species.createIndividual()));
					}
				}
			}
		});
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource rand = context.random();
		BlockPos pos = context.origin();

		// todo configurable
		float globalRarity = TreeConfig.getSpawnRarity();
		if (globalRarity <= 0.0F) {
			return false;
		}

		if (BIOME_CACHE.isEmpty()) {
			generateBiomeCache(level);
		}

		for (int tries = 0; tries < 4; tries++) {
			int x = pos.getX() + rand.nextInt(16);
			int z = pos.getZ() + rand.nextInt(16);

			Holder<Biome> biome = level.getBiome(pos);
			List<ITree> trees = BIOME_CACHE.computeIfAbsent(biome.unwrapKey().get(), k -> List.of());

			for (ITree tree : trees) {
				ITreeSpecies species = tree.getSpecies();
				if (species.getRarity() * globalRarity >= rand.nextFloat()) {
					BlockPos validPos = getValidPos(level, x, z, tree);
					if (validPos == null) {
						continue;
					}

					if (TreeGenHelper.generateTree(species, level, validPos)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
