package forestry.core.worldgen;

import forestry.Forestry;
import forestry.api.arboriculture.ITreeGenData;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.worldgen.ITreeBlockType;
import forestry.arboriculture.worldgen.TreeBlockType;
import forestry.arboriculture.worldgen.TreeBlockTypeLog;
import forestry.arboriculture.worldgen.TreeContour;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureHelper {
	public static boolean addBlock(LevelAccessor world, BlockPos pos, ITreeBlockType type, EnumReplaceMode replaceMode) {
		return addBlock(world, pos, type, replaceMode, TreeContour.EMPTY);
	}

	public static boolean addBlock(LevelAccessor world, BlockPos pos, ITreeBlockType type, EnumReplaceMode replaceMode, TreeContour contour) {
		if (!world.hasChunkAt(pos)) {
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		if (replaceMode.canReplace(blockState, world, pos)) {
			type.setBlock(world, pos);
			contour.addLeaf(pos);
			return true;
		}
		return false;
	}

	/**
	 * Uses centerPos and girth of a tree to calculate the center
	 */
	public static void generateCylinderFromTreeStartPos(LevelAccessor world, ITreeBlockType block, BlockPos startPos, int girth, float radius, int height, EnumReplaceMode replace, TreeContour contour) {
		generateCylinderFromPos(world, block, startPos.offset(girth / 2, 0, girth / 2), radius, 1f, height, replace, contour);
	}

	/**
	 * Center is the bottom middle of the cylinder
	 */
	public static void generateCylinderFromPos(LevelAccessor world, ITreeBlockType block, BlockPos center, float radius, int height, EnumReplaceMode replace, TreeContour contour) {
		generateCylinderFromPos(world, block, center, radius, 1f, height, replace, contour);
	}

	/**
	 * Uses centerPos and girth of a tree to calculate the center
	 */
	public static void generateCylinderFromTreeStartPos(LevelAccessor world, ITreeBlockType block, BlockPos startPos, int girth, float radius, float radiusMult, int height, EnumReplaceMode replace, TreeContour contour) {
		generateCylinderFromPos(world, block, startPos.offset(girth / 2, 0, girth / 2), radius, radiusMult, height, replace, contour);
	}

	/**
	 * Center is the bottom middle of the cylinder
	 */
	public static void generateCylinderFromPos(LevelAccessor world, ITreeBlockType block, BlockPos center, float radius, float radiusMult, int height, EnumReplaceMode replace, TreeContour contour) {
		BlockPos start = BlockPos.containing(center.getX() - radius, center.getY(), center.getZ() - radius);
		for (int x = 0; x < radius * 2 + 1; x++) {
			for (int y = height - 1; y >= 0; y--) { // generating top-down is faster for lighting calculations
				for (int z = 0; z < radius * 2 + 1; z++) {
					BlockPos position = start.offset(x, y, z);
					Vec3i treeCenter = new Vec3i(center.getX(), position.getY(), center.getZ());
					if (position.distSqr(treeCenter) <= ((radius * radius) + 0.01) * radiusMult) {
						Direction direction = VecUtil.direction(position, treeCenter);
						block.setDirection(direction);
						if (addBlock(world, position, block, replace)) {
							contour.addLeaf(position);
						}
					}
				}
			}
		}
	}

	/**
	 * Generates a cylinder with blocks on the perimeter having a chance to not be placed, for a bit of variation.
	 * Center is the bottom middle of the cylinder.
	 *
	 * @param failChance the chance that a block isn't placed. Values higher than 1 mean blocks closer to the centre begin to not be placed.
	 */
	public static void generateCylinderFromPosWithChance(LevelAccessor world, ITreeBlockType block, BlockPos center, float radius, float radiusMult, int height, EnumReplaceMode replace, TreeContour contour, RandomSource rand, float failChance) {
		BlockPos start = BlockPos.containing(center.getX() - radius, center.getY(), center.getZ() - radius);

		float maxDistSqr = ((radius * radius) + 0.01f) * radiusMult;
		float chance = failChance - (float) Math.floor(failChance);
		float randDist = maxDistSqr - (float) (Math.ceil(failChance) * Math.ceil(failChance));

		for (int x = 0; x < radius * 2 + 1; x++) {
			for (int y = height - 1; y >= 0; y--) { // generating top-down is faster for lighting calculations
				for (int z = 0; z < radius * 2 + 1; z++) {
					BlockPos position = start.offset(x, y, z);
					Vec3i treeCenter = new Vec3i(center.getX(), position.getY(), center.getZ());

					float curDistSqr = (float) position.distSqr(treeCenter);

					//First, check if the block is within radius
					if (curDistSqr <= maxDistSqr) {

						//Now, check based on noise.
						if (
							failChance <= 0 || //Always place if chance is 0 or less
								curDistSqr <= randDist || //block is below the noise threshold
								(
									//block is in noise threshold
									curDistSqr > randDist && chance <= rand.nextFloat()
								)
						) {
							Direction direction = VecUtil.direction(position, treeCenter);
							block.setDirection(direction);
							if (addBlock(world, position, block, replace)) {
								contour.addLeaf(position);
							}
						}
					}
				}
			}
		}
	}

	public static void generateCircleFromTreeStartPos(LevelAccessor world, RandomSource rand, BlockPos startPos, int girth, float radius, int width, int height, ITreeBlockType block, float chance, EnumReplaceMode replace, TreeContour contour) {
		generateCircle(world, rand, startPos.offset(girth / 2, 0, girth / 2), radius, width, height, block, chance, replace, contour);
	}

	public static void generateCircle(LevelAccessor world, RandomSource rand, BlockPos center, float radius, int width, int height, ITreeBlockType block, float chance, EnumReplaceMode replace, TreeContour contour) {
		BlockPos start = BlockPos.containing(center.getX() - radius, center.getY(), center.getZ() - radius);
		BlockPos area = BlockPos.containing(radius * 2 + 1, height, radius * 2 + 1);

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		for (int x = start.getX(); x < start.getX() + area.getX(); x++) {
			for (int y = start.getY() + area.getY() - 1; y >= start.getY(); y--) { // generating top-down is faster for lighting calculations
				for (int z = start.getZ(); z < start.getZ() + area.getZ(); z++) {

					if (rand.nextFloat() > chance) {
						continue;
					}

					double distance = mutablePos.set(x, y, z).distToLowCornerSqr(center.getX(), y, center.getZ());
					if ((radius - width - 0.01) * (radius - width - 0.01) < distance && distance <= (radius + 0.01) * (radius + 0.01)) {
						if (addBlock(world, mutablePos, block, replace)) {
							contour.addLeaf(mutablePos);
						}
					}
				}
			}
		}
	}

	public static void generateSphereFromTreeStartPos(LevelAccessor world, BlockPos startPos, int girth, int radius, ITreeBlockType block, EnumReplaceMode replace, TreeContour contour) {
		generateSphere(world, startPos.offset(girth / 2, 0, girth / 2), radius, block, replace, contour);
	}

	public static void generateSphere(LevelAccessor world, BlockPos center, int radius, ITreeBlockType block, EnumReplaceMode replace, TreeContour contour) {
		Vec3i start = new Vec3i(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
		Vec3i area = new Vec3i(radius * 2 + 1, radius * 2 + 1, radius * 2 + 1);
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		for (int x = start.getX(); x < start.getX() + area.getX(); x++) {
			for (int y = start.getY() + area.getY() - 1; y >= start.getY(); y--) { // generating top-down is faster for lighting calculations
				for (int z = start.getZ(); z < start.getZ() + area.getZ(); z++) {
					if (center.closerThan(mutablePos.set(x, y, z), radius + 0.01)) {
						if (addBlock(world, mutablePos, block, replace)) {
							contour.addLeaf(mutablePos);
						}
					}
				}
			}
		}
	}

	public static void generateEllipsoid(LevelAccessor world, BlockPos center, float radiusX, float radiusY, float radiusZ, ITreeBlockType block, EnumReplaceMode replace, TreeContour contour) {
		generateEllipsoid(world, center, radiusX, radiusY, radiusZ, 1, block, replace, contour);
	}

	/**
	 * @param world      The world to place the blocks in.
	 * @param center     Where the ellipsoid should be placed
	 * @param radiusX    The radius of the ellipsoid in the X direction
	 * @param radiusY    The radius of the ellipsoid in the Y direction
	 * @param radiusZ    The radius of the ellipsoid in the Z direction
	 * @param radiusMult How much to increase the size of the ellipsoid while keeping it contained within the bounds.
	 * @param block      The block being placed
	 * @param replace    The replacement mode
	 * @param contour    A container for branch ends and leaf positions
	 */
	public static void generateEllipsoid(LevelAccessor world, BlockPos center, float radiusX, float radiusY, float radiusZ, float radiusMult, ITreeBlockType block, EnumReplaceMode replace, TreeContour contour) {
		Vec3i start = new Vec3i(center.getX() - Math.round(radiusX), center.getY() - Math.round(radiusY), center.getZ() - Math.round(radiusZ));
		Vec3i area = new Vec3i((int) radiusX * 2 + 1, (int) radiusY * 2 + 1, (int) radiusZ * 2 + 1);

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		for (int x = start.getX() - 1; x <= start.getX() + area.getX(); x++) {
			for (int y = start.getY() + area.getY() + 1; y > start.getY(); y--) { // generating top-down is faster for lighting calculations
				for (int z = start.getZ() - 1; z <= start.getZ() + area.getZ(); z++) {

					if ((((x - center.getX()) * (x - center.getX())) / (radiusX * radiusX)
						+ ((y - center.getY()) * (y - center.getY())) / (radiusY * radiusY)
						+ ((z - center.getZ()) * (z - center.getZ())) / (radiusZ * radiusZ)) <= 1.00 * radiusMult) {

						mutablePos.set(x, y, z);
						if (addBlock(world, mutablePos, block, replace)) {
							contour.addLeaf(mutablePos);
						}
					}
				}
			}
		}
	}

	public static void generateLine(LevelAccessor world, BlockPos start, BlockPos end, float thicknessStart, float thicknessEnd, ITreeBlockType leaf, EnumReplaceMode replace, TreeContour contour) {

		//Differences between coordinate starts and finishes.
		float dx = end.getX() - start.getX();
		float dy = end.getY() - start.getY();
		float dz = end.getZ() - start.getZ();

		//Start by calculating the distance
		float length = (float) Math.sqrt(start.distSqr(end));
		if (length == 0) return;

		//Each 'step' should be a fraction of the full length
		Vec3 step = new Vec3(
			dx / length,
			dy / length,
			dz / length
		);
		float stepDist = (float) step.length();

		//Prog keeps track of where we are when 'building' the line
		Vec3 prog = new Vec3(0, 0, 0);
		BlockPos.MutableBlockPos mutablePos = start.mutable();

		for (float d = 0; d <= length; d += stepDist) {

			float completion = d / length;
			float thickness = thicknessStart + (thicknessEnd - thicknessStart) * completion;

			mutablePos = mutablePos.set(
				(int) (start.getX() + prog.x),
				(int) (start.getY() + prog.y),
				(int) (start.getZ() + prog.z)
			);
			generateEllipsoid(world, mutablePos, thickness, thickness, thickness, 1.5f + (completion * 0.5f), leaf, replace, contour);

			prog = prog.add(step);
		}

	}

	/**
	 * Updates logOrigins to contain the coordinates of the first block placed per y level in the tree.
	 */
	public static void generateTreeTrunk(
		LevelAccessor level,
		List<BlockPos> logOrigins,
		RandomSource rand,
		ITreeBlockType wood,
		BlockPos startPos,
		int height,
		int girth,
		int yStart,
		float vinesChance,
		@Nullable Direction leanDirection,
		float leanScale
	) {

		final int leanStartY = (int) Math.floor(height * 0.33f);
		int prevXOffset = 0;
		int prevZOffset = 0;

		int leanX = 0;
		int leanZ = 0;

		if (leanDirection != null) {
			leanX = leanDirection.getStepX();
			leanZ = leanDirection.getStepZ();
		}

		for (int x = 0; x < girth; x++) {
			for (int z = 0; z < girth; z++) {
				for (int y = height - 1; y >= yStart; y--) { // generating top-down is faster for lighting calculations
					float lean;
					if (y < leanStartY) {
						lean = 0;
					} else {
						lean = leanScale * (y - leanStartY) / (height - leanStartY);
					}
					int xOffset = (int) Math.floor(leanX * lean);
					int zOffset = (int) Math.floor(leanZ * lean);

					if (xOffset != prevXOffset || zOffset != prevZOffset) {
						prevXOffset = xOffset;
						prevZOffset = zOffset;
						if (y > 0) {
							if (leanDirection != null) {
								wood.setDirection(leanDirection);
							}
							addBlock(level, startPos.offset(x + xOffset, y - 1, z + zOffset), wood, EnumReplaceMode.ALL);
							wood.setDirection(Direction.UP);
						}
					}

					BlockPos pos = startPos.offset(x + xOffset, y, z + zOffset);
					addBlock(level, pos, wood, EnumReplaceMode.ALL);
					addVines(level, rand, pos, vinesChance);

					if (x == 0 && z == 0)
						logOrigins.add(pos);
				}
			}
		}
	}


	/**
	 * Updates logOrigins to contain the coordinates of the first block placed per y level in the tree.
	 * Takes a taper instead of a direction as I can't forsee needing a tree to do both.
	 * logOrigins, for sake of simplicity, will still pick the first x and z coordinate where a log SHOULD generate
	 * even if it doesn't. Most things won't be affected by this, but keep it in mind.
	 *
	 * @param taper the percentage representing at which point the tree should reach maximum girth.
	 */
	public static void generateTreeTrunk(
		LevelAccessor level,
		List<BlockPos> logOrigins,
		List<BlockPos> branchEnds,
		RandomSource rand,
		ITreeBlockType wood,
		BlockPos startPos,
		int height,
		int girth,
		int yStart,
		float vinesChance,
		float taper
	) {

		int taperStart = yStart + (int) (height * taper); // Work out the highest point that max girth occurs.

		for (int y = height - 1; y >= yStart; y--) { // generating top-down is faster for lighting calculations

			//The X and Z coordinates for the middle of the tree
			int midX = startPos.getX() + (girth / 2);
			int midZ = startPos.getZ() + (girth / 2);

			float taperAmount = (float) (y - taperStart) / (height - taperStart);

			for (int x = 0; x < girth; x++) {
				for (int z = 0; z < girth; z++) {
					BlockPos pos = startPos.offset(x, y, z);

					float dist = (float) Math.pow(pos.getX() - midX, 2) + (float) Math.pow(pos.getZ() - midZ, 2);
					float max = (float) Math.pow(girth * (1f - taperAmount), 2);

					Forestry.LOGGER.debug("Dist: " + dist + ", Max: " + max);
					//if the Y is below the start of the taper, or is within tapering distance
					if (y <= taperStart || dist <= max) {

						addBlock(level, pos, wood, EnumReplaceMode.ALL);
						addVines(level, rand, pos, vinesChance);
					}

					if (x == 0 && z == 0)
						logOrigins.add(pos);
				}
			}
		}
	}

	protected static void addVines(LevelAccessor world, RandomSource rand, BlockPos pos, float chance) {
		if (chance <= 0) {
			return;
		}

		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true);
			addBlock(world, pos.west(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true);
			addBlock(world, pos.east(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true);
			addBlock(world, pos.north(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true);
			addBlock(world, pos.south(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
	}

	public static void generatePods(IGenome genome, LevelAccessor world, RandomSource rand, BlockPos startPos, int height, int minHeight, int girth, TreeContour contour, EnumReplaceMode replaceMode) {
		for (BlockPos logPos : contour.getTrunkOrigins()) { // generating top-down is faster for lighting calculations
			// Only generate pods within valid height range
			int relativeY = logPos.getY() - startPos.getY();
			if (relativeY < minHeight) {
				continue;
			}

			for (int x = 0; x < girth; x++) {
				for (int z = 0; z < girth; z++) {
					// logic to skip over trying to spawn pods in the middle of a tree.
					if ((girth > 2) && (x > 0 && x < girth - 1) && (z > 0 && z < girth - 1)) {
						continue;
					}

					trySpawnFruitBlock(genome, world, rand, logPos.offset(x + 1, 0, z), replaceMode);
					trySpawnFruitBlock(genome, world, rand, logPos.offset(x - 1, 0, z), replaceMode);
					trySpawnFruitBlock(genome, world, rand, logPos.offset(x, 0, z + 1), replaceMode);
					trySpawnFruitBlock(genome, world, rand, logPos.offset(x, 0, z - 1), replaceMode);
				}
			}
		}
	}

	private static void trySpawnFruitBlock(IGenome genome, LevelAccessor world, RandomSource rand, BlockPos pos, EnumReplaceMode replaceMode) {
		BlockState blockState = world.getBlockState(pos);
		if (replaceMode.canReplace(blockState, world, pos)) {
			genome.getActiveSpecies().<ITreeSpecies>cast().trySpawnFruitBlock(genome, world, rand, pos);
		}
	}

	public static void generateSupportStems(ITreeBlockType wood, LevelAccessor world, RandomSource rand, BlockPos startPos, int height, int girth, float chance, float maxHeight) {

		final int min = -1;

		for (int x = min; x <= girth; x++) {
			for (int z = min; z <= girth; z++) {

				// skip the corners, support stems should touch the body of the trunk
				if ((x == min && z == min) || (x == girth && z == girth) || (x == min && z == girth) || (x == girth && z == min)) {
					continue;
				}

				int stemHeight = rand.nextInt(Math.round(height * maxHeight));
				if (rand.nextFloat() < chance) {
					for (int y = 0; y < stemHeight; y++) {
						addBlock(world, startPos.offset(x, y, z), wood, EnumReplaceMode.SOFT);
					}
				}
			}
		}
	}

	/**
	 * A new method for generating branches that is designed to be a little bit more reliable, primarily in the way branches spread out.
	 *
	 * @param world
	 * @param rand
	 * @param wood
	 * @param startPos
	 * @param girth
	 * @param spreadY
	 * @param spreadXZ
	 * @param radius
	 * @param count
	 * @param chance
	 * @return
	 */
	public static Set<BlockPos> generateBranches(final LevelAccessor world, final RandomSource rand, final ITreeBlockType wood, final BlockPos startPos, final int girth, final float spreadY, final float spreadXZ, int radius, final int count, final float chance) {
		Set<BlockPos> branchEnds = new HashSet<>();
		if (radius < 1) {
			radius = 1;
		}

		for (final Direction branchDirection : Direction.Plane.HORIZONTAL) {
			wood.setDirection(branchDirection);

			BlockPos branchStart = startPos;

			int offsetX = branchDirection.getStepX();
			int offsetZ = branchDirection.getStepZ();
			if (offsetX > 0) {
				branchStart = branchStart.offset(girth - offsetX, 0, 0);
			}
			if (offsetZ > 0) {
				branchStart = branchStart.offset(0, 0, girth - offsetZ);
			}

			boolean firstStep = true;

			//We generate 'count' branches in every direction, with a chance of failure
			for (int i = 0; i < count; i++) {
				if (rand.nextFloat() > chance) {
					continue;
				}
				int y = 0;
				int x = 0;
				int z = 0;

				BlockPos branchEnd = null;

				//Determines if X and Z should lean left-right
				//This stops branches doubling back on themselves.
				boolean xDir = rand.nextBoolean();
				boolean zDir = rand.nextBoolean();

				//Used to force branches to spread in a certain direction after a certain distance.
				//Hopefully prevents really long branches that extend out in one direction
				float yForce = 0;
				float xzForce = 0;

				for (int r = 0; r < radius; r++) {
					//Stop the very first step being upwards - it's not very branchlike
					if ((rand.nextFloat() < spreadY || yForce >= 1) && !firstStep) {
						// make branches only spread up, not down
						y++;
						wood.setDirection(Direction.UP);

						if (yForce >= 1)
							yForce = yForce % 1;
						else
							yForce = 0;

					} else {

						if (!firstStep)
							yForce += spreadY;

						firstStep = false;

						if (rand.nextFloat() < spreadXZ || xzForce >= 1) {

							if (xzForce >= 1)
								xzForce = xzForce % 1;
							else
								xzForce = 0;

							if (branchDirection.getAxis() == Direction.Axis.Z) {
								if (xDir) {
									x++;
								} else {
									x--;
								}
								wood.setDirection(Direction.EAST);
							} else if (branchDirection.getAxis() == Direction.Axis.X) {
								if (zDir) {
									z++;
								} else {
									z--;
								}
								wood.setDirection(Direction.SOUTH);
							}
						} else {
							x += offsetX;
							z += offsetZ;
							wood.setDirection(branchDirection);

							xzForce += spreadXZ;
						}
					}

					BlockPos pos = branchStart.offset(x, y, z);
					if (addBlock(world, pos, wood, EnumReplaceMode.SOFT)) {
						branchEnd = pos;
					} else {
						break;
					}
				}

				if (branchEnd != null) {
					branchEnds.add(branchEnd);
				}
			}
		}

		return branchEnds;
	}

	/**
	 * Returns the respective wood block when provided with a log and wood type
	 * Used for generation to ensure wood types respect a trees fireproof status
	 *
	 * @param log
	 * @param woodType
	 * @return
	 */
	public static TreeBlockType getWoodFromLog(TreeBlockTypeLog log, ForestryWoodType woodType) {
		if (log.getGenome().getActiveValue(TreeChromosomes.FIREPROOF))
			return new TreeBlockType(ArboricultureBlocks.WOOD_FIREPROOF.get(woodType).defaultState());
		else
			return new TreeBlockType(ArboricultureBlocks.WOOD.get(woodType).defaultState());
	}

	public enum EnumReplaceMode {
		AIR {
			@Override
			public boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
				return world.isEmptyBlock(pos);
			}
		},
		ALL {
			@Override
			public boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
				return true;
			}
		},
		SOFT {
			@Override
			public boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
				if (world instanceof Level) {
					BlockPlaceContext context = new DirectionalPlaceContext((Level) world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP);
					return blockState.canBeReplaced(context);
				}
				return blockState.canBeReplaced();
			}
		};

		public abstract boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos);
	}

	public static class DirectionHelper {
		public static final Direction[] VALUES = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

		public static Direction getRandom(RandomSource random) {
			return VALUES[random.nextInt(VALUES.length)];
		}

		public static Direction getRandomOther(RandomSource random, Direction direction) {
			List<Direction> directions = Arrays.asList(VALUES);
			directions.remove(direction);
			int size = directions.size();
			return directions.toArray(new Direction[size])[random.nextInt(size)];
		}
	}
}
