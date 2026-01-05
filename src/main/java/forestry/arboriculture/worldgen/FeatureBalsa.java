package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeatureBalsa extends FeatureTree {

	public FeatureBalsa(ITreeGenData tree) {
		super(tree, 6, 6);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		BlockPos topPos = startPos.offset(0, this.height + 1, 0);
		BlockPos.MutableBlockPos leafCenter = new BlockPos.MutableBlockPos();
		float leafRadius = (this.girth - 1.0f) / 2.0f;

		FeatureHelper.addBlock(level, leafCenter.set(topPos), leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		leafCenter.move(Direction.DOWN);
		FeatureHelper.generateCylinderFromPos(level, leaf, leafCenter, leafRadius + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		leafCenter.move(Direction.DOWN);

		if (this.height > 10) {
			FeatureHelper.generateCylinderFromPos(level, leaf, leafCenter, leafRadius + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			leafCenter.move(Direction.DOWN);
		}

		while (leafCenter.getY() > topPos.getY() - 6) {
			FeatureHelper.generateCylinderFromPos(level, leaf, leafCenter, leafRadius + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			leafCenter.move(Direction.DOWN);
		}
	}
}
