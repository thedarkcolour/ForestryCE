package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureDogwood extends FeatureTree {
	public FeatureDogwood(ITreeGenData tree) {
		super(tree, 5, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		Set<BlockPos> branchPos = new HashSet<>();


		for (int y = this.height - 2; y >= 2; y -= 2) {
			int branchRadius = this.height / 2;

			if (y > this.height / 2)
				branchRadius = (int) Math.max(2, branchRadius * 0.7f);

			branchPos.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, y, 0), this.girth, 0.2f, 0.25f, branchRadius, 1, 0.5f));
		}

		return branchPos;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd.offset(0, 2, 0), 2, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2, 1.5f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 1;

		float radius = 1;
		int end = rand.nextInt(1, 3);
		float radiusMod = 0.4f;
		if (end > 2) radiusMod = 0.5f;

		if ((this.height - end) * radiusMod > 4.5f) // prevent tall trees from having too wide a canopy and despawning too many leaves
			radiusMod = 4.5f / this.height;

		while (leafSpawn > end) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, radius + Math.min(this.girth, 2), 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			radius += radiusMod;
		}
	}
}
