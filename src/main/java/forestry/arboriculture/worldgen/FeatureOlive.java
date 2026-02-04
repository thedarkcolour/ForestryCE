package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeatureOlive extends FeatureTree {
	public FeatureOlive(ITreeGenData tree) {
		super(tree, 5, 4);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float radius = 2 + (int) (this.girth / 2f);

		// Main Canopy
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height, this.girth / 2), radius, 1, radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int leafSpawn = this.height - 1;
		int i = 0;

		while (leafSpawn >= 2) {
			int gMod = Math.round(this.girth / 2f); //omg garry's modification reference????

			int randX = rand.nextIntBetweenInclusive(-gMod, gMod);
			int randZ = rand.nextIntBetweenInclusive(-gMod, gMod);

			if (leafSpawn == 2) {
				randX /= 2;
				randZ /= 2;
			}

			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(randX, leafSpawn, randZ), this.girth, radius, 1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

			if (i >= (this.girth / 2 + 1)) {
				i = 0;
				leafSpawn--;
			} else {
				i++;
			}
		}
	}
}
