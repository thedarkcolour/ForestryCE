package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.Set;

public class FeatureWillow extends FeatureTree {

	public FeatureWillow(ITreeGenData tree) {
		super(tree, 5, 2);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0.8f, null, 0);
		FeatureHelper.generateSupportStems(wood, level, rand, startPos, this.height, this.girth, 0.2f, 0.2f);

		int leafSpawn = this.height - 4;
		while (leafSpawn > 2) {
			// support branches for tall willows, keeps the leaves from decaying immediately
			if ((leafSpawn - 3) % 6 == 0) {
				FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, leafSpawn, 0), this.girth, 0, 0, 2, 1, 1.0f);
			}
			leafSpawn--;
		}

		return Collections.emptySet();
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 3f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 3f + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		while (leafSpawn > 2) {
			FeatureHelper.generateCircleFromTreeStartPos(level, rand, startPos.offset(0, leafSpawn--, 0), this.girth, 4f, 2, 1, leaf, 1.0f, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		FeatureHelper.generateCircleFromTreeStartPos(level, rand, startPos.offset(0, leafSpawn--, 0), this.girth, 4f, 1, 1, leaf, 1.0f, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCircleFromTreeStartPos(level, rand, startPos.offset(0, leafSpawn--, 0), this.girth, 4f, 1, 1, leaf, 1.0f, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCircleFromTreeStartPos(level, rand, startPos.offset(0, leafSpawn, 0), this.girth, 4f, 1, 1, leaf, 0.4f, FeatureHelper.EnumReplaceMode.AIR, contour);
	}
}
