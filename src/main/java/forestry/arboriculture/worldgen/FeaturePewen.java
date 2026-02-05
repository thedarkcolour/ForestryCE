package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeaturePewen extends FeatureTree {
	public FeaturePewen(ITreeGenData tree) {
		super(tree, 12, 5);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 1, 0), this.girth, 0f, 0.0f, 2, 4, 1.0f); //Supports the top canopy

		if (this.height > 8) {
			int branchY = this.height - rand.nextIntBetweenInclusive(5, 7);
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchY, 0), this.girth, 0.4f, 0.25f, 2, 1, 1.0f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		// Generate top-most blob
		for (int i = 2; i >= 0; i--) {
			//float radMult = 1.5f-(i/2f);
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, (i - 2) + this.height, 0), this.girth, 5f - i, 1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		// Generate smaller blob for branches
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			for (int i = 2; i >= 0; i--) {
				FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd.offset(0, i, 0), 2.5f - i, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			}
		}
	}
}
