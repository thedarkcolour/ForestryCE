package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureGreenheart extends FeatureTree {
	public FeatureGreenheart(ITreeGenData tree) {
		super(tree, 10, 8);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0.4f, null, 0);
		FeatureHelper.generateSupportStems(wood, level, rand, startPos, this.height, this.girth, 0.5f, 0.2f);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 0.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		while (leafSpawn > this.height - 4) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.9f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, 0.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		if (this.height > 10) {
			// Add some smaller twigs below for flavour
			for (int times = 0; times < this.height / 4; times++) {
				int h = 10 + rand.nextInt(this.height - 10);
				if (rand.nextBoolean() && h < this.height / 2) {
					h = this.height / 2 + rand.nextInt(this.height / 2);
				}
				int x_off = -1 + rand.nextInt(3);
				int y_off = -1 + rand.nextInt(3);
				FeatureHelper.generateSphere(level, startPos.offset(x_off, h, y_off), 1 + rand.nextInt(1), leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
			}
		}
	}
}
