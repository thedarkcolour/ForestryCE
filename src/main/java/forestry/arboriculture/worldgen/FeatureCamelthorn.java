package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureCamelthorn extends FeatureTree {
	public FeatureCamelthorn(ITreeGenData tree) {
		super(tree, 5, 2);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height - 2, this.girth, 0, 0, null, 0);

		Set<BlockPos> branches = new HashSet<>();

		FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 3, 0), this.girth, 0.5f, 0.15f, 3, 1, 1);

		int y = this.height - 5;

		if (this.height > 7) {
			while (y >= 3) {

				branches.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, y, 0), this.girth, 0.25f, 0.3f, 3, 1, 0.5f));

				y -= rand.nextIntBetweenInclusive(3, 5);
			}
		}

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (int y = 1; y <= 3; y++) {
			// These numbers may seem as if they're arbitrary. That's because they are.
			float rad = (4f + (this.girth / 1.5f)) * (1.2f - (1f / (y)));
			float radMult = 1.125f + (rand.nextFloat() / 2f);

			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, this.height + 1 - y, 0), this.girth, rad, radMult, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		for (BlockPos blockPos : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, blockPos.offset(0, +1, 0), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, blockPos, 2f, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
