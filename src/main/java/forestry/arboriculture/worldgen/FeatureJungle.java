package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class FeatureJungle extends FeatureTreeVanilla {
	public FeatureJungle(ITreeGenData tree) {
		super(tree);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		int height = this.height;
		float vinesChance = 0.0f;
		if (this.girth >= 2) {
			// todo does this multiplication actually do anything?
			height *= 1.5f;
			vinesChance = 0.8f;
		}

		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, height, this.girth, 0, vinesChance, null, 0);

		if (height > 10) {
			int branchSpawn = 6;
			while (branchSpawn < height - 2) {
				branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchSpawn, 0), this.girth, 0.5f, 0f, 2, 1, 0.25f));
				branchSpawn += rand.nextInt(4);
			}
		}

	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int height = this.height;
		if (this.girth >= 2) {
			// todo does this multiplication actually do anything?
			height *= 1.5f;
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height + 1;
		float canopyRadiusMultiplier = height / 7.0f;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 0.5f * canopyRadiusMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.9f * canopyRadiusMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, 1.9f * canopyRadiusMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
