package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureElm extends FeatureTree {
	public FeatureElm(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int trunkSpawn = this.height - 3;
		float adjustedGirth = this.girth * .75f;

		while (trunkSpawn > 3) {
			int radius = (int) Math.round(adjustedGirth + (this.height - trunkSpawn) * 1.2);
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn, 0), this.girth, 0.2f, 0.3f, radius, 1, 0.85f));
			trunkSpawn -= 2;
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;
		float adjustedGirth = this.girth * .75f;

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn -= 1, this.girth / 2), this.girth + 2.25f, 2, this.girth + 2.25f, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn -= 2, this.girth / 2), this.girth + 3.875f, 2.5f, this.girth + 3.875f, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd, 2f + this.girth, 2, 2f + this.girth, .9f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(0, -2, 0), 3f + this.girth, 2, 3f + this.girth, .9f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
