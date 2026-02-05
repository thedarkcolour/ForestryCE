package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureBeech extends FeatureTree {
	public FeatureBeech(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int radius = Math.round((float) this.girth / 5 + 1.5f);

		int maxBranchHeight = rand.nextInt(3, 5);

		for (int yBranch = this.height - 1; yBranch > maxBranchHeight; yBranch--) {
			branchCoords.addAll(
				FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.15f, 0.25f, radius, 1, 0.5f)
			);
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int r = 3;
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(this.girth / 2, -1, this.girth / 2), r, 2, r, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		int yCenter = this.height - this.girth;
		yCenter = yCenter > 3 ? yCenter : 4;
		FeatureHelper.generateEllipsoid(level, startPos.offset(0, yCenter, 0), r, 3 + rand.nextInt(this.height / 3), r, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
