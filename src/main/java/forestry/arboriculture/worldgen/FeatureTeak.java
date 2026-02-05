package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureTeak extends FeatureTree {
	public FeatureTeak(ITreeGenData tree) {
		super(tree, 7, 4, 5);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchWidth = (this.height / 3) - 1;

		if (this.height > 4)
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 3, 0), this.girth, 0.2f, 0.33f, branchWidth, 1, 0.5f));

		if (this.height > 6)
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 5, 0), this.girth, 0.2f, 0.2f, branchWidth, 1, 0.75f));

	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float r = 3 + (this.girth / 2f);
		float ri = r / 2;

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height + 1, this.girth / 2), ri, 1.5f, ri, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int leafSpawn = this.height;

		float radMult = 1.5f;

		while (leafSpawn >= (this.height / 5) * 3) {
			radMult /= 2;
			if (radMult <= 0.05f) break;

			float ro = r * (1 - radMult);

			FeatureHelper.generateEllipsoid(level, startPos.offset((this.girth / 2), leafSpawn--, (this.girth / 2)), ro, 1.5f, ro, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			//float lRadius = (rand.nextFloat() * 0.5f) + 1.25f;
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(0, 1, 0), 1, 1.5f, 1, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(0, 0, 0), 2, 1.5f, 2, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
