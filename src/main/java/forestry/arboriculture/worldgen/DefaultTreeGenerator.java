package forestry.arboriculture.worldgen;

import com.google.common.base.Preconditions;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.arboriculture.ITreeGenerator;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.TreeManager;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.genetics.Tree;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.tiles.TileUtil;

public class DefaultTreeGenerator implements ITreeGenerator {
	private final Function<ITreeGenData, Feature<NoneFeatureConfiguration>> factory;
	private final IWoodType woodType;

	public DefaultTreeGenerator(Function<ITreeGenData, Feature<NoneFeatureConfiguration>> factory, IWoodType woodType) {
		this.factory = factory;
		this.woodType = Preconditions.checkNotNull(woodType);
	}

	@Override
	public Feature<NoneFeatureConfiguration> getTreeFeature(ITreeGenData tree) {
		return this.factory.apply(tree);
	}

	@Override
	public boolean setLogBlock(IGenome genome, LevelAccessor level, BlockPos pos, Direction facing) {
		boolean fireproof = genome.getActiveValue(TreeChromosomes.FIREPROOF);
		BlockState logBlock = TreeManager.woodAccess.getBlock(woodType, WoodBlockKind.LOG, fireproof);

		Direction.Axis axis = facing.getAxis();
		return level.setBlock(pos, logBlock.setValue(RotatedPillarBlock.AXIS, axis), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_ALL);
	}

	@Override
	public boolean setLeaves(IGenome genome, LevelAccessor level, BlockPos pos, RandomSource rand) {
		if (genome.isDefaultGenome() && level instanceof WorldGenLevel) {
			return this.woodType.setDefaultLeaves(level, pos, genome, rand, null);
		} else {
			BlockState leaves = ArboricultureBlocks.LEAVES.defaultState();
			boolean placed = level.setBlock(pos, LeavesBlock.updateDistance(leaves, level, pos), 19);
			if (!placed) {
				return false;
			}

			TileLeaves tileLeaves = TileUtil.getTile(level, pos, TileLeaves.class);
			if (tileLeaves == null) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 19);
				return false;
			}

			Tree tree = new Tree(genome);
			tileLeaves.setTree(tree);
			tileLeaves.setFruit(tree, false);
			return true;
		}
	}
}
