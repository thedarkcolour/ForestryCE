package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureSourCherry extends FeatureTree {
	public FeatureSourCherry(ITreeGenData tree) {
		super(tree, 5, 4);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchHeight = this.height - 1;
		int branchWidth = this.height / 2;
		while (branchHeight > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchHeight, 0), this.girth, 0.2f, 0.5f, branchWidth, 1, 1.0f));
			branchHeight -= 2;
			branchWidth++;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height;

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), (this.girth / 2f) + 2, 1.5f, (this.girth / 2f) + 2, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd, 2, 1.75f, 2, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
