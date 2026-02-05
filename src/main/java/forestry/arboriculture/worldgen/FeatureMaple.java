package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureMaple extends FeatureTree {
	public FeatureMaple(ITreeGenData tree) {
		super(tree, 7, 5);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		for (int yBranch = 2; yBranch < this.height - 2; yBranch++) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.15f, 0.25f, Math.round((this.height - yBranch) * 0.25f), 1, 0.25f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2 + this.girth, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 1;
		float diameterchange = (float) 1 / this.height;
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
