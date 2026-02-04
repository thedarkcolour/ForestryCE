package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureBaobab extends FeatureTree {
	public FeatureBaobab(ITreeGenData tree) {
		super(tree, 6, 6);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height - 1, this.girth, 0, 0, null, 0);

		if (rand.nextFloat() < 0.3f) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, wood, startPos.offset(0, this.height - 1, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, TreeContour.EMPTY);
		} else if (rand.nextBoolean()) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, wood, startPos.offset(0, this.height - 1, this.girth / 2), this.girth, this.girth - 1, 1, FeatureHelper.EnumReplaceMode.SOFT, TreeContour.EMPTY);
		}

		branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 2, 0), this.girth, 0, 0.5f, 4, 6, 1.0f));
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, this.girth, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, 1f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		// Add tree top
		for (int times = 0; times < this.height / 2; times++) {
			int h = this.height - 1 + rand.nextInt(4);
			if (rand.nextBoolean() && h < this.height / 2) {
				h = this.height / 2 + rand.nextInt(this.height / 2);
			}

			int x_off = -this.girth + rand.nextInt(2 * this.girth);
			int y_off = -this.girth + rand.nextInt(2 * this.girth);

			BlockPos center = startPos.offset(x_off, h, y_off);
			int radius = 1;
			if (this.girth > 1) {
				radius += rand.nextInt(this.girth - 1);
			}
			FeatureHelper.generateSphere(level, center, radius, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		// Add some smaller twigs below for flavour
		for (int times = 0; times < this.height / 4; times++) {
			int delim = modifyByHeight(level, 6, 0, this.height);
			int h = delim + (delim < this.height ? rand.nextInt(this.height - delim) : 0);
			if (rand.nextBoolean() && h < this.height / 2) {
				h = this.height / 2 + rand.nextInt(this.height / 2);
			}
			int x_off = -1 + rand.nextInt(3);
			int y_off = -1 + rand.nextInt(3);

			BlockPos center = startPos.offset(x_off, h, y_off);
			int radius = 1 + rand.nextInt(2);
			FeatureHelper.generateSphere(level, center, radius, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
