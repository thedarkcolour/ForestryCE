package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeaturePlum extends FeatureTree {

	public FeaturePlum(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int yCenter = this.height - this.girth;
		yCenter = yCenter > 2 ? yCenter : 3;

		int radius = Math.round((2 + rand.nextInt(this.girth)) * (this.height / 4.0f));
		if (radius > 4) {
			radius = 4;
		}
		FeatureHelper.generateSphereFromTreeStartPos(level, startPos.offset(0, yCenter, 0), this.girth, radius, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
	}
}
