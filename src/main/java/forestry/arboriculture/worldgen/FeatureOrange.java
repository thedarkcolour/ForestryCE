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
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		Set<BlockPos> branches = new HashSet<>();

		int branchSpawn = height - 1;
		int branchCount = 1;
		float heightIncreasePercent = height / 3f;

		do {
			branches.addAll(FeatureHelper.generateBranches(level, rand, wood,
				startPos.offset(0, branchSpawn, 0),
				girth,
				0.4f, 0.15f,
				(girth / 3) + branchCount,
				2, 0.75f));
			branchCount++;
			branchSpawn -= 3;
		} while (branchSpawn >= 2);

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		/*float radius = (float)Math.ceil(girth/1.5f)+1;
		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, height, girth/2), radius, 1f, radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		if (height >= 5) {
			FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, (2*height)/3, girth/2), radius+1, height/3f, radius+1, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		}

		int spawnrange = height+1-rand.nextIntBetweenInclusive(1,2); //leave 1-2 blocks of clearance at the base of the tree
		int leafspawn = height+1;

		float topSpawnRange = (2f*spawnrange)/5;
		float bottomSpawnRange = (3f*spawnrange)/5;

		float radius = (float)Math.ceil(girth/1.5f);*/

		float heightIncreasePercent = height / 3f;

		float radius = (float) Math.ceil(girth / 1.5f) + 1;
		FeatureHelper.generateEllipsoid(level, startPos.offset(girth / 2, height, girth / 2), radius, 1f + Math.min(heightIncreasePercent - 1, 1), radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateEllipsoid(level, startPos.offset(girth / 2, height - (int) heightIncreasePercent, girth / 2), radius, heightIncreasePercent, radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, heightIncreasePercent + 0.5f, 1.25f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
