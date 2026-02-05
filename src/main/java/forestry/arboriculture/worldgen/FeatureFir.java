package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureFir extends FeatureTree {
	private static final int MIN_HEIGHT = 3;

	public FeatureFir(ITreeGenData tree) {
		super(tree, 7, 5);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, branchCoords, rand, wood, startPos, Math.max(this.height - this.girth, MIN_HEIGHT), this.girth, 0, 0, 0.4f);

		for (int yBranch = 3; yBranch < this.height - (this.height / 2); yBranch++) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.05f, 0.1f, Math.round((this.height - yBranch) * 0.15f), 1, 0.33f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateSphere(level, branchEnd, 2, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + this.girth + 1;

		float maxRadius = 2.25f + rand.nextFloat();
		maxRadius *= Math.min(1f, this.height / 6f); //Shrink the width of smaller trees

		//determines the rate of radius change as Y decreases.
		float step = maxRadius / this.height;
		float r = 0;

		//step *= (girth);
		int canopyHeight = rand.nextIntBetweenInclusive(1, 2);

		while (leafSpawn > canopyHeight) {
			r += step;
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, r, (4f / 3), 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, (r * 0.75f), 1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
