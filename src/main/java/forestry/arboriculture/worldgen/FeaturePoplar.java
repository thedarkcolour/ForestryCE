package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeaturePoplar extends FeatureTree {

	public FeaturePoplar(ITreeGenData tree) {
		super(tree, 8, 3);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;
		float leafRadius = (this.girth - 1.0f) / 2.0f;

		while (leafSpawn > this.girth - 1) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, leafRadius + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
