package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureWenge extends FeatureTree {
	public FeatureWenge(ITreeGenData tree) {
		super(tree, 6, 2);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		float branchSize = 3;
		int branchHeight = this.height + 1;
		while (branchHeight > 1) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchHeight, 0), this.girth, 0.2f, 0.2f, (int) branchSize, 2, 0.75f));
			branchHeight--;
			branchSize += 0.5f;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 0.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		while (leafSpawn > 1) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 3f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			leafSpawn--;
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCircle(level, rand, branchEnd, 3, 3, 2, leaf, 1.0f, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
