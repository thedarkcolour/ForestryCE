package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureFeijoa extends FeatureTree {
	public FeatureFeijoa(ITreeGenData tree) {
		super(tree, 4, 1, 2);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		float chance = 0.75f;
		if (this.height >= 3) {
			chance = 0.5f;
		}

		for (int y = this.height - 1; y >= 1; y--) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, y, 0), this.girth, 0, 0.25f, this.girth / 3, 2, chance));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1.5f, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height;

		int radius = (int) Math.ceil((float) this.girth / 2) + 1;
		do {
			FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), radius, 1f, radius, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		} while (leafSpawn > 2);
	}
}
