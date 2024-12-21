package forestry.arboriculture.worldgen;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

public class FeatureCherryVanilla extends FeatureTree {
	private static final float WIDE_BOTTOM_LAYER_HOLE_CHANCE = 0.25f;
	private static final float CORNER_HOLE_CHANCE = 0.25f;
	private static final float HANGING_LEAVES_CHANCE = 1f / 6f;
	private static final float HANGING_LEAVES_EXTENSION_CHANCE = 1f / 3f;

	public FeatureCherryVanilla(ITreeGenData data) {
		super(data, 7, 1);
	}

	// todo support custom girth
	// Based off of CherryTrunkPlacer
	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		wood.setDirection(Direction.UP);

		int i = Math.max(2, this.height - 1 + rand.nextIntBetweenInclusive(-4, -3));
		int j = Math.max(1, this.height - 5);
		if (j >= i) {
			++j;
		}

		int branchCount = 1 + rand.nextInt(3);
		boolean middleBranch = branchCount == 3;
		boolean multipleBranches = branchCount >= 2;
		int l;

		if (middleBranch) {
			l = this.height;
		} else if (multipleBranches) {
			l = Math.max(i, j) + 1;
		} else {
			l = i + 1;
		}

		BlockPos.MutableBlockPos cursor = startPos.mutable();
		for (int i1 = 0; i1 < l; ++i1) {
			FeatureHelper.addBlock(level, cursor, wood, FeatureHelper.EnumReplaceMode.ALL);
			cursor.move(0, 1, 0);
		}

		Set<BlockPos> branchPositions = new HashSet<>();
		if (middleBranch) {
			branchPositions.add(cursor.offset(0, 1, 0));
		}
		cursor.set(startPos);
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(rand);

		branchPositions.add(generateBranch(level, rand, startPos, wood, direction, i, i < l - 1, cursor));
		if (multipleBranches) {
			branchPositions.add(generateBranch(level, rand, startPos, wood, direction.getOpposite(), j, j < l - 1, cursor));
		}

		return branchPositions;
	}

	private BlockPos generateBranch(LevelAccessor level, RandomSource rand, BlockPos pos, TreeBlockTypeLog wood, Direction horizontal, int pSecondBranchStartOffsetFromTop, boolean isLow, BlockPos.MutableBlockPos cursor) {
		cursor.set(pos).move(Direction.UP, pSecondBranchStartOffsetFromTop);
		int i = this.height - 1 + rand.nextIntBetweenInclusive(-1, 0);
		boolean lowBranch = isLow || i < pSecondBranchStartOffsetFromTop;
		int j = rand.nextIntBetweenInclusive(2, 4) + (lowBranch ? 1 : 0);
		BlockPos blockpos = pos.relative(horizontal, j).above(i);
		int k = lowBranch ? 2 : 1;
		wood.setDirection(horizontal);

		for (int l = 0; l < k; ++l) {
			FeatureHelper.addBlock(level, cursor.move(horizontal), wood, FeatureHelper.EnumReplaceMode.ALL);
		}

		Direction vertical = blockpos.getY() > cursor.getY() ? Direction.UP : Direction.DOWN;

		while (true) {
			int distance = cursor.distManhattan(blockpos);
			if (distance == 0) {
				return blockpos.above();
			}

			float upwardChance = (float) Math.abs(blockpos.getY() - cursor.getY()) / (float) distance;
			boolean upwardGrowth = rand.nextFloat() < upwardChance;
			cursor.move(upwardGrowth ? vertical : horizontal);
			wood.setDirection(upwardGrowth ? vertical : horizontal);
			FeatureHelper.addBlock(level, cursor, wood, FeatureHelper.EnumReplaceMode.ALL);
		}
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int foliageHeight = 5;
		int foliageRadius = 4;
		boolean wideTrunk = this.girth >= 2;

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			int i = foliageRadius - 1;

			placeLeavesRow(level, rand, leaf, contour, branchEnd, i - 2, foliageHeight - 3, wideTrunk);
			placeLeavesRow(level, rand, leaf, contour, branchEnd, i - 1, foliageHeight - 4, wideTrunk);
			placeLeavesRow(level, rand, leaf, contour, branchEnd, i, 0, wideTrunk);

			placeLeavesRowWithHangingLeavesBelow(level, rand, leaf, contour, branchEnd, i, -1, wideTrunk);
			placeLeavesRowWithHangingLeavesBelow(level, rand, leaf, contour, branchEnd, i - 1, -2, wideTrunk);
		}
	}

	// Copy of FoliagePlacer.placeLeavesRow
	private void placeLeavesRow(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos pos, int pRange, int y, boolean wideTrunk) {
		int i = wideTrunk ? 1 : 0;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int x = -pRange; x <= pRange + i; ++x) {
			for (int z = -pRange; z <= pRange + i; ++z) {
				if (!shouldSkipLocationSigned(rand, x, y, z, pRange, wideTrunk)) {
					cursor.setWithOffset(pos, x, y, z);
					FeatureHelper.addBlock(level, cursor, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
				}
			}
		}
	}

	protected boolean shouldSkipLocationSigned(RandomSource pRandom, int x, int y, int z, int pRange, boolean wideTrunk) {
		int adjustedX;
		int adjustedZ;

		if (wideTrunk) {
			adjustedX = Math.min(Math.abs(x), Math.abs(x - 1));
			adjustedZ = Math.min(Math.abs(z), Math.abs(z - 1));
		} else {
			adjustedX = Math.abs(x);
			adjustedZ = Math.abs(z);
		}

		return shouldSkipLocation(pRandom, adjustedX, y, adjustedZ, pRange);
	}

	// From CherryFoliagePlacer
	protected boolean shouldSkipLocation(RandomSource rand, int x, int y, int z, int range) {
		if (y == -1 && (x == range || z == range) && rand.nextFloat() < WIDE_BOTTOM_LAYER_HOLE_CHANCE) {
			return true;
		} else {
			boolean isEdge = x == range && z == range;
			boolean wide = range > 2;
			if (wide) {
				return isEdge || x + z > range * 2 - 2 && rand.nextFloat() < CORNER_HOLE_CHANCE;
			} else {
				return isEdge && rand.nextFloat() < CORNER_HOLE_CHANCE;
			}
		}
	}

	protected void placeLeavesRowWithHangingLeavesBelow(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos pos, int range, int y, boolean wideTrunk) {
		placeLeavesRow(level, rand, leaf, contour, pos, range, y, wideTrunk);
		int i = wideTrunk ? 1 : 0;
		BlockPos below = pos.below();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Direction right = direction.getClockWise();
			int j = right.getAxisDirection() == Direction.AxisDirection.POSITIVE ? range + i : range;
			cursor.setWithOffset(pos, 0, y - 1, 0).move(right, j).move(direction, -range);
			int k = -range;

			while (k < range + i) {
				boolean hasLeafAbove = contour.hasLeaf(cursor.move(Direction.UP));
				cursor.move(Direction.DOWN);

				if (hasLeafAbove) {
					if (cursor.distManhattan(below) < 7 && rand.nextFloat() <= HANGING_LEAVES_CHANCE) {
						FeatureHelper.addBlock(level, cursor, leaf, FeatureHelper.EnumReplaceMode.AIR);

						cursor.move(Direction.DOWN);
						if (cursor.distManhattan(below) < 7 && rand.nextFloat() <= HANGING_LEAVES_EXTENSION_CHANCE) {
							FeatureHelper.addBlock(level, cursor, leaf, FeatureHelper.EnumReplaceMode.AIR);
						}
						cursor.move(Direction.UP);

					}
				}

				++k;
				cursor.move(direction);
			}
		}
	}
}
