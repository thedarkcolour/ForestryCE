package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureSequoia extends FeatureTree {
	public FeatureSequoia(ITreeGenData tree) {
		this(tree, 20, 5);
	}

	protected FeatureSequoia(ITreeGenData tree, int baseHeight, int heightVariation) {
		super(tree, baseHeight, heightVariation);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		FeatureHelper.generateSupportStems(wood, level, rand, startPos, this.height, this.girth, 0.4f, 0.4f);

		int topHeight = this.height / 3 + rand.nextInt(this.height / 6);

		for (int yBranch = topHeight; yBranch < this.height; yBranch++) {
			int branchLength = Math.round(this.height - yBranch) / 2;
			if (branchLength > 4) {
				branchLength = 4;
			}
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.05f, 0.25f, branchLength, 1, 0.5f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1.0f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 2;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int topHeight = this.height / 3 + rand.nextInt(this.height / 6);
		while (leafSpawn > topHeight) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
