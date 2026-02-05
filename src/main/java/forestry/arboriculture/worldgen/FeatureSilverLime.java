package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureSilverLime extends FeatureTree {
	public FeatureSilverLime(ITreeGenData tree) {
		super(tree, 6, 4);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		BlockPos pos = startPos.offset(0, 3 + rand.nextInt(1), 0);
		branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, pos, this.girth, 0.25f, 0.10f, Math.round(this.height * 0.25f), 2, 0.5f));
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		float radius = 1;
		while (leafSpawn > 1) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, radius + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			radius += 0.25;
		}
	}
}
