package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureIpe extends FeatureTree {

	public FeatureIpe(ITreeGenData tree) {
		super(tree, 6, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int trunkSpawn = this.height - 2;
		float adjustedGirth = this.girth * .65f;

		Set<BlockPos> branchCoords = new HashSet<>();
		while (trunkSpawn > 2) {
			int radius = Math.round(adjustedGirth * (this.height - trunkSpawn) / 1.5f);
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn, 0), this.girth, 0.25f, 0.25f, radius, 2, 1.0f));
			trunkSpawn -= 2;
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;
		float adjustedGirth = this.girth * .65f;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 0.2f * adjustedGirth + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, 0.2f * adjustedGirth + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2.0f + this.girth, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
