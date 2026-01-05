package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

public class FeatureCocobolo extends FeatureTree {

	public FeatureCocobolo(ITreeGenData tree) {
		super(tree, 8, 8);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		return FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height;

		for (BlockPos treeTop : contour.getBranchEnds()) {
			FeatureHelper.addBlock(level, treeTop.above(), leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
		leafSpawn--;
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		if (this.height > 10) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		leafSpawn--;

		while (leafSpawn > 4) {
			int offset = 1;
			if (rand.nextBoolean()) {
				offset = -1;
			}

			float radius = (leafSpawn % 2 == 0) ? 2 + this.girth : this.girth;
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(offset, leafSpawn, offset), this.girth, radius, 1, FeatureHelper.EnumReplaceMode.AIR, contour);

			leafSpawn--;
		}
	}
}
