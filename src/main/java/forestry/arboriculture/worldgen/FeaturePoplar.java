package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeaturePoplar extends FeatureTree {
	public FeaturePoplar(ITreeGenData tree) {
		super(tree, 8, 3);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;
		int leafRadius = (this.girth / 2) + 1;

		while (leafSpawn > this.girth - 1) {

			float failChance = 0.3f;

			if (leafSpawn >= this.height - 1)
				failChance = 0.6f;

			if (leafSpawn == this.height + 1)
				failChance = 0.99f;

			FeatureHelper.generateCylinderFromPosWithChance(level, leaf, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), leafRadius, 2f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour, rand, failChance);
		}
	}
}
