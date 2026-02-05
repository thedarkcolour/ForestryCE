package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureWalnut extends FeatureTree {
	public FeatureWalnut(ITreeGenData tree) {
		super(tree, 7, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchHeight = this.height - 3;
		float branchSize = 2;
		while (branchHeight >= 3) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchHeight, 0), this.girth, 0.2f, 0.2f, (int) branchSize, 1, 0.5f));
			branchHeight--;
			branchSize += 0.25f;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 0.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		float branchSize = 2;
		while (leafSpawn >= 3) {
			int leafRadius = Math.min(4, (int) branchSize);
			for (BlockPos branchEnd : contour.getBranchEnds()) {
				FeatureHelper.generateCircle(level, rand, branchEnd, leafRadius, 2, 2, leaf, 1.0f, FeatureHelper.EnumReplaceMode.SOFT, contour);
			}
			leafSpawn--;
			branchSize += 0.25f;
		}
	}
}
