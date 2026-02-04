package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeaturePapaya extends FeatureTree {
	public FeaturePapaya(ITreeGenData tree) {
		super(tree, 7, 2);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 2, 0), this.girth, 0.5f, 0.1f, 4, 1, 1f));
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd, 2f, 1.5f, 2f, 1.25f, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		float r = 3f + (this.girth / 2);

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height + 1, this.girth / 2), r, 1.5f, r, 1.25f, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
	}
}
