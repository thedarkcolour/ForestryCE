package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureOrange extends FeatureTree {
	public FeatureOrange(ITreeGenData tree) {
		super(tree, 3, 3, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);

		Set<BlockPos> branches = new HashSet<>();

		int branchSpawn = this.height - 1;
		int branchCount = 1;
		float heightIncreasePercent = this.height /3f;

		do {

			branches.addAll(FeatureHelper.generateBranches(level, rand, wood,
					startPos.offset(0, branchSpawn, 0),
				this.girth,
					0.4f, 0.15f,
					(this.girth /3)+branchCount,
					2, 0.75f));
			branchCount++;
			branchSpawn-=3;
		} while (branchSpawn >= 2);

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float heightIncreasePercent = this.height /3f;

		float radius = (float)Math.ceil(this.girth /1.5f)+1;
		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth /2, this.height, this.girth /2), radius, 1f+ Math.min(heightIncreasePercent-1, 1)  , radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateEllipsoid(level, startPos.offset(this.girth /2, this.height -(int)heightIncreasePercent, this.girth /2), radius, heightIncreasePercent, radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd: contour.getBranchEnds()){
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, heightIncreasePercent+0.5f, 1.25f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
