package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureGinkgo extends FeatureTree {
	public FeatureGinkgo(ITreeGenData tree) {
		super(tree, 7, 4);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int trunkSpawn = this.height - 1;
		float baseRad = 1f;
		float radMod = 2f;

		while (trunkSpawn > 2) {

			float radius = baseRad + (1.0f - (float) trunkSpawn / this.height) * radMod;
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn, 0), this.girth, 0.15f, 0.3f, (int) radius, 1, 0.25f));
			trunkSpawn -= 3;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 2;

		int end = rand.nextIntBetweenInclusive(1, 2);
		int rAdd = 0;

		float baseRad = 1f;
		float radMod = 2f;

		while (leafSpawn > end) {
			// Basically this makes a slightly conic cylinder, where the top is 2 blocks thinner than the base.
			float radius = baseRad + (1.0f - (float) leafSpawn / this.height) * radMod + rAdd + ((this.girth - 1f) / 2);

			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, radius, 1.25f, 1, FeatureHelper.EnumReplaceMode.AIR, contour);

			rAdd = (rAdd == 0) ? 1 : 0;
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2f, 1.25f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
