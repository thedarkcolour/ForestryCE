package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureMacrocarpa extends FeatureTree {
	public FeatureMacrocarpa(ITreeGenData tree) {
		super(tree, 7, 7);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		//Direction d = FeatureHelper.DirectionHelper.getRandom(rand);
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchesEnd = 2;
		int y = this.height - 3;

		while (y >= branchesEnd) {
			int depth = this.height - y;
			branchCoords.addAll(
				FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, y, 0), this.girth, 0.35f, 0.4f, (int) (depth / 1.5f) + (int) Math.ceil(this.girth / 2f), 2, 1)
			);
			y -= 3;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		// Top of Tree
		float r = (this.girth / 2f) + 2.5f;
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - 1, this.girth / 2), r, 2f, r, 1.2f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		// End of Branches
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2f, 1.4f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
