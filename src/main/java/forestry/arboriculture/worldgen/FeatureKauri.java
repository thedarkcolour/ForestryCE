package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.arboriculture.ForestryWoodType;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureKauri extends FeatureTree {
	public FeatureKauri(ITreeGenData tree) {
		super(tree, 15, 5);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		TreeBlockType bark = FeatureHelper.getWoodFromLog(wood, ForestryWoodType.KAURI);

		FeatureHelper.generateSupportStems(bark, level, rand, startPos, this.height, this.girth, 0.8f, 0.2f);

		int count = rand.nextIntBetweenInclusive((int) (this.girth * 4.5f), (int) (this.girth * 6.5f));
		int branchWidth = (int) (this.height / 2f);

		while (branchCoords.size() <= count) {
			// Make a nest of branches at the top of the tree. Account for very small trees.
			int branchPos = rand.nextIntBetweenInclusive(Math.max(this.height - 8, 2), this.height);

			// branches closer to the top tend to climb upward more
			float spreadMod = 0.15f * (branchPos / (float) this.height);

			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, bark, startPos.offset(0, branchPos, 0), this.girth, 0.2f + spreadMod, 0.4f, branchWidth, 1, 0.5f));
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos blockPos : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, blockPos.offset(0, 1, 0), 2, 2, 2, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, blockPos.offset(0, -1, 0), 2, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, this.height + 1, this.girth / 2), 2 + (this.girth / 2f), 2, 2 + (this.girth / 2f), 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset(this.girth / 2, this.height - 1, this.girth / 2), 2 + (this.girth / 2f), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
