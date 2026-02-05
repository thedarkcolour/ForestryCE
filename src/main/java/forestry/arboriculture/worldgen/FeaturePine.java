package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeaturePine extends FeatureTree {
	public FeaturePine(ITreeGenData tree) {
		super(tree, 11, 4);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int vRadius = (int) (this.height * 0.4f);

		// Make the initial leaf body - this is mainly to make the point
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - 2, this.girth / 2), 1 + (this.girth / 2f), vRadius, 1 + (this.girth / 2f), leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		// Make the 'layers'
		int leafSpawn = this.height + 1;
		for (int y = 0; y < (vRadius * 2) - 6; y += 2) {
			FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), 1 + (this.girth / 2f), 1, 1 + (this.girth / 2f), leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), 2 + (this.girth / 2f), 1, 2 + (this.girth / 2f), leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		// Add the last little ring at the base of the canopy
		leafSpawn--;
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), 0.5f + (this.girth / 2f), 1, 0.5f + (this.girth / 2f), leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), 1 + (this.girth / 2f), 1, 1 + (this.girth / 2f), leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
