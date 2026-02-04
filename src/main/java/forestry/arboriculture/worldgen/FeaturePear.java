package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeaturePear extends FeatureTree {
	public FeaturePear(ITreeGenData tree) {
		super(tree, 5, 3, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, (int) Math.max(this.height * 0.4f, 2), 0), this.girth, 0.15f, 0.15f, Math.round(this.height / 5f), 2, 0.75f));
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1, 2f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height - 1;

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - 1, this.girth / 2), this.girth, 2, this.girth, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int end = rand.nextIntBetweenInclusive(1, 2);
		float heightMult = Math.max(this.height / 5f, 1);
		// give taller trees thicker foliage
		float radius = heightMult + (this.girth / 2f);

		while (leafSpawn >= end) {
			int randX = rand.nextIntBetweenInclusive(-1, 1);
			int randZ = rand.nextIntBetweenInclusive(-1, 1);

			if (leafSpawn == this.height - 1 || leafSpawn == end) {
				randX = 0;
				randZ = 0;
			}

			FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset((this.girth / 2) + randX, leafSpawn, (this.girth / 2) + randZ), radius, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

			leafSpawn--;
		}
	}
}
