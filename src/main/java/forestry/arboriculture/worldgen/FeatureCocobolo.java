package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeatureCocobolo extends FeatureTree {
	public FeatureCocobolo(ITreeGenData tree) {
		super(tree, 8, 8);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
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
