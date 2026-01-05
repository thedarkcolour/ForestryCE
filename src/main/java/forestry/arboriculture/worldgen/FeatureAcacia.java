package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureAcacia extends FeatureTree {
	public FeatureAcacia(ITreeGenData tree) {
		super(tree, 5, 2);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		Direction leanDirection = FeatureHelper.DirectionHelper.getRandom(rand);
		float leanAmount = this.height / 4.0f;

		Set<BlockPos> treeTops = FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, leanDirection, leanAmount);
		if (this.height > 5 && rand.nextBoolean()) {
			Direction branchDirection = FeatureHelper.DirectionHelper.getRandomOther(rand, leanDirection);
			Set<BlockPos> treeTops2 = FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, Math.round(this.height * 0.66f), this.girth, 0, 0, branchDirection, leanAmount);
			treeTops.addAll(treeTops2);
		}

		Set<BlockPos> branchEnds = new HashSet<>();

		for (BlockPos treeTop : treeTops) {
			int xOffset = treeTop.getX();
			int yOffset = treeTop.getY() - startPos.getY() + 1;
			int zOffset = treeTop.getZ();
			float canopyMultiplier = (1.5f * this.height - yOffset + 2) / 4.0f;
			int canopyThickness = Math.max(1, Math.round(yOffset / 10.0f));

			branchEnds.add(new BlockPos(xOffset, startPos.getY() + yOffset--, zOffset));
			yOffset--;

			float canopyWidth = rand.nextBoolean() ? 3.0f : 2.5f;
			int radius = Math.round(canopyMultiplier * canopyWidth - 4);
			BlockPos pos = new BlockPos(xOffset, startPos.getY() + yOffset - canopyThickness, zOffset);
			branchEnds.addAll(FeatureHelper.generateBranches(level, rand, wood, pos, this.girth, 0.0f, 0.1f, radius, 2, 1.0f));
		}

		return branchEnds;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			int leafSpawn = branchEnd.getY() - startPos.getY();
			int canopyThickness = Math.max(1, Math.round(leafSpawn / 10.0f));
			float canopyMultiplier = (1.5f * this.height - leafSpawn + 2) / 4.0f;
			float canopyWidth = rand.nextBoolean() ? 1.0f : 1.5f;
			BlockPos center = new BlockPos(branchEnd.getX(), leafSpawn - canopyThickness + 1 + startPos.getY(), branchEnd.getZ());
			float radius = Math.max(1, canopyMultiplier * canopyWidth + this.girth);
			FeatureHelper.generateCylinderFromPos(level, leaf, center, radius, canopyThickness, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
