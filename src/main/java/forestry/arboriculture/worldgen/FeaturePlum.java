package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeaturePlum extends FeatureTree {
	public FeaturePlum(ITreeGenData tree) {
		super(tree, 4, 4);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchHeight = this.height - 1;
		int branchWidth = this.height / 4;

		while (branchHeight > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchHeight, 0), this.girth, 0.2f, 0.5f, branchWidth, 1, 1.0f));
			branchHeight -= 2;
			//branchWidth++;
			// first (top-most) set of branches are shorter than the rest
			branchWidth = this.height / 2;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 2;

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn -= 2, this.girth / 2), (this.girth / 2f) + 2, 1.5f, (this.girth / 2f) + 2, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int dx = 0;
		int dz = 0;

		while (leafSpawn > 4) {
			FeatureHelper.generateEllipsoid(level, startPos.offset((this.girth / 2) + dx, leafSpawn -= 2, (this.girth / 2) + dz), (this.girth / 2f) + 3, 1.5f, (this.girth / 2f) + 3, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

			dx = rand.nextIntBetweenInclusive(-1, 1);
			dz = rand.nextIntBetweenInclusive(-1, 1);
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd, 2, 1.5f, 2, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
