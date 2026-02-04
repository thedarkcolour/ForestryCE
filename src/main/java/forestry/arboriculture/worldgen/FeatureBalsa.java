package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeatureBalsa extends FeatureTree {

	public FeatureBalsa(ITreeGenData tree) {
		super(tree, 6, 6);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		int leafRadius = (this.girth / 2) + 1;

		float heightMult = (this.height / 6f); //Taller trees have a longer canopy

		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), this.girth / 2f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int canopyLength = (int) Math.min(Math.max((4 * heightMult), 4), 8);

		while (canopyLength > 0) {

			float failChance = 0.45f;
			FeatureHelper.generateCylinderFromPosWithChance(level, leaf, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), leafRadius, 2f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour, rand, failChance);

			canopyLength--;
		}

	}
}
