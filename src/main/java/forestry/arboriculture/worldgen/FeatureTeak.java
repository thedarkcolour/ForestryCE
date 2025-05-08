package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureTeak extends FeatureTree {
	public FeatureTeak(ITreeGenData tree) {
		super(tree, 8, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchWidth = (this.height / 3) - 1;

		Set<BlockPos> branches = new HashSet<>(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 2, 0), this.girth, 0.35f, 0.33f, branchWidth, 1, 0.5f));

		if (this.height > 8) {
			branches.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 5, 0), this.girth, 0.2f, 0.2f, branchWidth, 1, 0.75f));
		}

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float r = 2 + (this.girth / 2f);

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height, this.girth / 2), r - 1, 1.5f, r - 1, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		if (this.height > 4) {
			FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - 2, this.girth / 2), r, 2.5f, r, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			float lRadius = (rand.nextFloat() * 0.5f) + 1.25f;
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(0, 1, 0), 2, 1.5f, 2, lRadius - 0.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateEllipsoid(level, branchEnd, 2, 1.5f, 2, lRadius, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
