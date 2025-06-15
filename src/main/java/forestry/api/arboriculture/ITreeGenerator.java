package forestry.api.arboriculture;

import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Implements the tree generation for a tree species.
 */
public interface ITreeGenerator {
	Feature<NoneFeatureConfiguration> getTreeFeature(ITreeSpecies tree);

	boolean setLogBlock(IGenome genome, LevelAccessor level, BlockPos pos, Direction facing);

	boolean setLeaves(IGenome genome, LevelAccessor level, BlockPos pos, RandomSource rand);
}
