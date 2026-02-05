package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureMahoe extends FeatureTree {
	public FeatureMahoe(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		for (int yBranch = 2; yBranch < this.height - 1; yBranch++) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.15f, 0.25f, Math.round((this.height - yBranch) * 0.75f), 1, 0.25f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2 + this.girth, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int yCenter = this.height - this.girth;
		yCenter = yCenter > 3 ? yCenter : 4;
		FeatureHelper.generateSphereFromTreeStartPos(level, startPos.offset(0, yCenter, 0), this.girth, 3 + rand.nextInt(this.girth), leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
	}
}
