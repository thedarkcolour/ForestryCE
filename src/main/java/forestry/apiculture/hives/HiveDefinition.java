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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.Tags;

import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.apiculture.hives.IHiveGen;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.apiculture.blocks.BlockHiveType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.core.utils.SpeciesUtil;

// todo this should be data driven
public enum HiveDefinition implements IHiveDefinition {
	FOREST(ApicultureBlocks.BEEHIVE.get(BlockHiveType.FOREST).defaultState(), 6.0f, ForestryBeeSpecies.FOREST, HiveGenTree.INSTANCE) {
		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			postGenFlowers(level, rand, pos, flowerStates);
		}

		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			//TODO: Forest bees now have slight cold tolerance. This tag restricts them to the warmer side. Should they require deciduous trees? investigate its not excluding the wrong biomes
			return super.isGoodBiome(biome) && !biome.is(Tags.Biomes.IS_SNOWY);
		}
	},
	MEADOWS(ApicultureBlocks.BEEHIVE.get(BlockHiveType.MEADOWS).defaultState(), 1.0f, ForestryBeeSpecies.MEADOWS, new HiveGenGround(BlockTags.DIRT)) {
		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			postGenFlowers(level, rand, pos, flowerStates);
		}

		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			//TODO: find a good way to exclude meadows bee from forested areas. This tag seems to contain temperate forests. Sometimes they still generate in plain old forests for some reason but are rarer
			return super.isGoodBiome(biome) && !biome.is(BiomeTags.IS_FOREST);
		}
	},
	DESERT(ApicultureBlocks.BEEHIVE.get(BlockHiveType.DESERT).defaultState(), 1.0f, ForestryBeeSpecies.MODEST, new HiveGenGround(ForestryTags.Blocks.MODEST_BEE_GROUND)) {
		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			postGenFlowers(level, rand, pos, cactusStates);
		}
	},
	JUNGLE(ApicultureBlocks.BEEHIVE.get(BlockHiveType.JUNGLE).defaultState(), 6.0f, ForestryBeeSpecies.TROPICAL, HiveGenTree.INSTANCE),
	END(ApicultureBlocks.BEEHIVE.get(BlockHiveType.END).defaultState(), 2.0f, ForestryBeeSpecies.ENDED, new HiveGenGround(ForestryTags.Blocks.ENDED_BEE_GROUND)) {
		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			return biome.is(BiomeTags.IS_END);
		}
	},
	SNOW(ApicultureBlocks.BEEHIVE.get(BlockHiveType.SNOW).defaultState(), 2.0f, ForestryBeeSpecies.WINTRY, new HiveGenGround(ForestryTags.Blocks.WINTRY_BEE_GROUND)) {
		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			BlockPos posAbove = pos.above();
			if (level.isEmptyBlock(posAbove)) {
				level.setBlock(posAbove, Blocks.SNOW.defaultBlockState(), Block.UPDATE_CLIENTS);
			}

			postGenFlowers(level, rand, pos, flowerStates);
		}
	},
	SWAMP(ApicultureBlocks.BEEHIVE.get(BlockHiveType.SWAMP).defaultState(), 2.0f, ForestryBeeSpecies.MARSHY, new HiveGenGround(BlockTags.DIRT)) {
		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			postGenFlowers(level, rand, pos, mushroomStates);
		}

		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			//No swamp bees bellow freezing
			return super.isGoodBiome(biome) && !biome.is(Tags.Biomes.IS_SNOWY);
		}
	},
	SAVANNA(ApicultureBlocks.BEEHIVE.get(BlockHiveType.SAVANNA).defaultState(), 1.0f, ForestryBeeSpecies.SAVANNA, new HiveGenGround(BlockTags.DIRT)) {
		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			//TODO: generate pumpkins in dry biomes and melons in normal ones
			//postGenFlowers(world,rand,pos,flowerStates);
		}
	},
	LUSH(ApicultureBlocks.BEEHIVE.get(BlockHiveType.LUSH).defaultState(), 2.0F, ForestryBeeSpecies.LUSH, new HiveGenCaveCeiling(ForestryTags.Blocks.LUSH_BEE_CEILING, ForestryTags.Blocks.CAVE_EXTRA_REPLACEABLES)) {
		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			return super.isGoodBiome(biome) && biome.is(Tags.Biomes.IS_CAVE);
		}

		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			if(level.getBlockState(pos.below()).canBeReplaced()){
				level.setBlock(pos.below(),Blocks.CAVE_VINES.defaultBlockState().setValue(BlockStateProperties.BERRIES, rand.nextFloat()<0.11F), Block.UPDATE_CLIENTS);
			}
		}
	},
	AQUATIC(ApicultureBlocks.BEEHIVE.get(BlockHiveType.AQUATIC).defaultState(), 1.0F, ForestryBeeSpecies.AQUATIC, new HiveGenOcean(BlockTags.SAND)){
		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			return biome.is(Biomes.WARM_OCEAN);
		}

		@Override
		public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
			for(Direction direction:Direction.VALUES){
				BlockPos pos2=pos.relative(direction);
				Block[] blocks=new Block[]{Blocks.FIRE_CORAL_WALL_FAN,Blocks.BRAIN_CORAL_WALL_FAN,Blocks.BUBBLE_CORAL_WALL_FAN,Blocks.HORN_CORAL_WALL_FAN,Blocks.TUBE_CORAL_WALL_FAN};
				if(direction.getAxis().isHorizontal()&&level.getBlockState(pos2).getBlock()==Blocks.WATER){
					level.setBlock(pos2, blocks[rand.nextInt(5)].defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction), Block.UPDATE_CLIENTS);
				}
				if(level.getBlockState(pos.above()).getBlock()==Blocks.WATER){
					Block[] blocks2=new Block[]{Blocks.FIRE_CORAL_FAN,Blocks.BRAIN_CORAL_FAN,Blocks.BUBBLE_CORAL_FAN,Blocks.HORN_CORAL_FAN,Blocks.TUBE_CORAL_FAN};
					level.setBlock(pos.above(), blocks2[rand.nextInt(5)].defaultBlockState(), Block.UPDATE_CLIENTS);
				}
			}
		}
	},
	NETHER(ApicultureBlocks.BEEHIVE.get(BlockHiveType.NETHER).defaultState(), 4.0F, ForestryBeeSpecies.EMBITTERED, new HiveGenCaveCeiling(BlockTags.WART_BLOCKS, ForestryTags.Blocks.NETHER_EXTRA_REPLACEABLES)){
		@Override
		public boolean isGoodBiome(Holder<Biome> biome) {
			return biome.is(BiomeTags.IS_NETHER);
		}
	};

	private static final IHiveGen FLOWER_GROUND = new HiveGenGround(ForestryTags.Blocks.PLANTABLE_FLOWERS_GROUND);
	private static final List<BlockState> flowerStates = new ArrayList<>();
	private static final List<BlockState> mushroomStates = new ArrayList<>();
	private static final List<BlockState> cactusStates = Collections.singletonList(Blocks.CACTUS.defaultBlockState());

	static {
		flowerStates.addAll(Blocks.POPPY.getStateDefinition().getPossibleStates());
		flowerStates.addAll(Blocks.DANDELION.getStateDefinition().getPossibleStates());
		mushroomStates.add(Blocks.RED_MUSHROOM.defaultBlockState());
		mushroomStates.add(Blocks.BROWN_MUSHROOM.defaultBlockState());
	}

	private final BlockState blockState;
	private final float genChance;
	private final ResourceLocation speciesId;
	private final IHiveGen hiveGen;

	HiveDefinition(BlockState hiveState, float genChance, ResourceLocation beeTemplate, IHiveGen hiveGen) {
		this.blockState = hiveState;
		this.genChance = genChance;
		this.speciesId = beeTemplate;
		this.hiveGen = hiveGen;
	}

	@Override
	public IHiveGen getHiveGen() {
		return hiveGen;
	}

	@Override
	public BlockState getBlockState() {
		return blockState;
	}

	@Override
	public boolean isGoodBiome(Holder<Biome> biome) {
		return !biome.is(BiomeTags.IS_NETHER);
	}

	@Override
	public boolean isGoodHumidity(HumidityType humidity) {
		IBeeSpecies species = SpeciesUtil.getBeeSpecies(this.speciesId);
		HumidityType idealHumidity = species.getHumidity();
		ToleranceType humidityTolerance = species.getDefaultGenome().getActiveValue(BeeChromosomes.HUMIDITY_TOLERANCE);
		return ClimateHelper.isWithinLimits(humidity, idealHumidity, humidityTolerance);
	}

	@Override
	public boolean isGoodTemperature(TemperatureType temperature) {
		IBeeSpecies species = SpeciesUtil.getBeeSpecies(this.speciesId);
		TemperatureType idealTemperature = species.getTemperature();
		ToleranceType temperatureTolerance = species.getDefaultGenome().getActiveValue(BeeChromosomes.TEMPERATURE_TOLERANCE);
		return ClimateHelper.isWithinLimits(temperature, idealTemperature, temperatureTolerance);
	}

	@Override
	public float getGenChance() {
		return genChance;
	}

	@Override
	public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
	}

	protected static void postGenFlowers(WorldGenLevel world, RandomSource rand, BlockPos hivePos, List<BlockState> flowerStates) {
		int plantedCount = 0;
		for (int i = 0; i < 10; i++) {
			int xOffset = rand.nextInt(8) - 4;
			int zOffset = rand.nextInt(8) - 4;
			BlockPos blockPos = hivePos.offset(xOffset, 0, zOffset);
			if ((xOffset == 0 && zOffset == 0) || !world.hasChunkAt(blockPos)) {
				continue;
			}

			blockPos = FLOWER_GROUND.getPosForHive(world, blockPos.getX(), blockPos.getZ());
			if (blockPos == null) {
				continue;
			}

			BlockState state = flowerStates.get(rand.nextInt(flowerStates.size()));
			Block block = state.getBlock();
			if (!block.defaultBlockState().canSurvive(world, blockPos)) {
				continue;
			}

			world.setBlock(blockPos, state, Block.UPDATE_CLIENTS);
			plantedCount++;

			if (plantedCount >= 3) {
				break;
			}
		}
	}
}
