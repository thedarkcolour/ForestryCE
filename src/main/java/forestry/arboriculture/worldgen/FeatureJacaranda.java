package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureJacaranda extends FeatureTree {
	public FeatureJacaranda(ITreeGenData tree) {
		super(tree, 5, 2);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int trunkSpawn = this.height - 1;

		while (trunkSpawn > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn -= 1, 0), this.girth, 0.5f, 0.3f, this.height / 2, 1, 0.5f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float r = 2f + (float) Math.ceil(this.girth / 1.5f);
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - 1, this.girth / 2), r, 2, r, 1.3f, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {

			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2.5f, 1.25f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
