package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureSpruce extends FeatureTree {

	public FeatureSpruce(ITreeGenData tree) {
		super(tree, 5, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		Set<BlockPos> branchEnds = new HashSet<>();

		int branchSpawn = this.height - 1;
		int branchWidth = this.height / 4;
		while (branchSpawn > 2) {
			branchEnds.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchSpawn, 0), this.girth, 0.2f, 0.5f, branchWidth, 1, 1.0f));
			branchSpawn -= 2;
			branchWidth++;
		}

		return branchEnds;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, (float) 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int leafRadius = 4;
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCircle(level, rand, branchEnd, leafRadius, 3, 2, leaf, 1.0f, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, (float) 2 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, (float) 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
