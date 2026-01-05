package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class FeaturePadauk extends FeatureTree {

	public FeaturePadauk(ITreeGenData tree) {
		super(tree, 6, 6);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchSpawn = this.height - 2;

		int count = 0;
		int max = 3;
		int min = 1;
		int canopyHeight = rand.nextInt(max - min + 1) + min;

		while (branchSpawn > 3 && count < canopyHeight) {
			count++;
			//Random Trunk Branches
			for (int i = 0; i < this.girth * 4; i++) {
				if (rand.nextBoolean()) {

					int[] offset = {-1, 1};
					int offsetValue = offset[new Random().nextInt(offset.length)];
					int maxBranchLength = 3;
					int branchLength = new Random().nextInt(maxBranchLength + 1);
					Direction[] direction = {Direction.NORTH, Direction.EAST};
					Direction directionValue = direction[new Random().nextInt(direction.length)];
					int branchSpawnY = branchSpawn;

					for (int j = 1; j < branchLength + 1; j++) {
						if (j == branchLength && rand.nextBoolean()) { //Just adding a bit of variation to the ends for character
							branchSpawnY += 1;
						}

						wood.setDirection(directionValue);
						if (directionValue == Direction.NORTH) {
							FeatureHelper.addBlock(level, startPos.offset(0, branchSpawnY, j * offsetValue), wood, FeatureHelper.EnumReplaceMode.ALL);
						} else if (directionValue == Direction.EAST) {
							FeatureHelper.addBlock(level, startPos.offset(j * offsetValue, branchSpawnY, 0), wood, FeatureHelper.EnumReplaceMode.ALL);
						}
					}
				}
			}
		}
		return Collections.emptySet();
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 3f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int count = 0;
		int max = 3;
		int min = 1;
		int canopyHeight = rand.nextInt(max - min + 1) + min;

		while (leafSpawn > 3 && count < canopyHeight) {
			int yCenter = leafSpawn--;
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, yCenter, 0), this.girth, 4.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			count++;
		}
	}
}
