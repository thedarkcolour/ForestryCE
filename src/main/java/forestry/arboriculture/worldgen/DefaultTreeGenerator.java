package forestry.arboriculture.worldgen;

import com.google.common.base.Preconditions;
import forestry.api.IForestryApi;
import forestry.api.arboriculture.*;
import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.genetics.Tree;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.tiles.TileUtil;
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

import java.util.function.Function;

public class DefaultTreeGenerator implements ITreeGenerator {
	private final Function<ITreeSpecies, Feature<NoneFeatureConfiguration>> factory;
	private final IWoodType woodType;

	public DefaultTreeGenerator(Function<ITreeSpecies, Feature<NoneFeatureConfiguration>> factory, IWoodType woodType) {
		this.factory = factory;
		this.woodType = Preconditions.checkNotNull(woodType);
	}

	@Override
	public Feature<NoneFeatureConfiguration> getTreeFeature(ITreeSpecies tree) {
		return this.factory.apply(tree);
	}

	@Override
	public boolean setLogBlock(IGenome genome, LevelAccessor level, BlockPos pos, Direction facing) {
		boolean fireproof = genome.getActiveValue(TreeChromosomes.FIREPROOF);
		BlockState logBlock = IForestryApi.INSTANCE.getTreeManager().getWoodAccess().getBlock(this.woodType, WoodBlockKind.LOG, fireproof);

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
