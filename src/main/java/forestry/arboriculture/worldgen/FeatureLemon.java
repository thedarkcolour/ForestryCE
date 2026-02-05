package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureLemon extends FeatureTree {
	public FeatureLemon(ITreeGenData tree) {
		super(tree, 3, 2, 2);
	}

	// Generation code has been copy-pasted from Orange trees, which is fine because they're so closely related.
	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		int branchSpawn = this.height - 1;
		int branchCount = 1;
		float heightIncreasePercent = this.height / 3f;

		do {
			// try place more branches at the lower canopies
			float branchChance = 0.75f;
			if (branchSpawn >= 4 && branchSpawn <= 6) branchChance = 0.9f;

			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood,
				startPos.offset(0, branchSpawn, 0),
				this.girth,
				0.4f, 0.15f,
				(this.girth / 3) + (int) (branchCount * (1 + (heightIncreasePercent - 1))),
				2, branchChance));
			branchCount++;
			branchSpawn -= 2;

			if (branchSpawn < 4 && branchCount == 2 && this.height > 6) branchSpawn = 4;
		} while (branchSpawn >= 4);

	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float heightIncreasePercent = this.height / 3f;

		float radius = (float) Math.ceil(this.girth / 1.5f) + 1;
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - (int) heightIncreasePercent, this.girth / 2), radius, 1.5f + Math.min(heightIncreasePercent - 1, 1), radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height - (int) heightIncreasePercent, this.girth / 2), radius, heightIncreasePercent, radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, heightIncreasePercent + 0.5f, 1f, 1 + (int) heightIncreasePercent, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
