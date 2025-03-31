package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

public class FeatureLemon extends FeatureTree {
	public FeatureLemon(ITreeGenData tree) {
		super(tree, 3, 3, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);


		return FeatureHelper.generateBranches(level, rand, wood,
			startPos.offset(0, this.height / 2, 0),
			this.girth,
			0, 0.25f,
			(this.girth / 3) + 1,
			2, 0.75f);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height - 1;

		int radius = (int) Math.ceil(this.girth / 2f) + 1;
		do {
			FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth / 2, leafSpawn--, this.girth / 2), radius, 1f, radius, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		} while (leafSpawn > 2);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1 + (int) (this.girth / 2f), 2 + ((this.girth - 1) / 2), FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
