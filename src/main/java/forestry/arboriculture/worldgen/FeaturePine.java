package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeaturePine extends FeatureTree {

	public FeaturePine(ITreeGenData tree) {
		super(tree, 6, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		Set<BlockPos> branchEnds = new HashSet<>();
		for (int yBranch = 2; yBranch < this.height - 2; yBranch++) {
			branchEnds.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.05f, 0.1f, Math.round((this.height - yBranch) * 0.25f), 1, 0.25f));
		}
		return branchEnds;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2 + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 1;
		float diameterchange = 1.25f / this.height;
		int leafSpawned = 2;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, (float) 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		while (leafSpawn > 1) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 3 * diameterchange * leafSpawned + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2 * diameterchange * leafSpawned + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			leafSpawned += 2;
		}
	}
}
