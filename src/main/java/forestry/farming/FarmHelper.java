package forestry.farming;

import forestry.api.farming.*;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;

public class FarmHelper {
	public static final Comparator<ICrop> TOP_DOWN_COMPARATOR = (o1, o2) -> VecUtil.TOP_DOWN_COMPARATOR.compare(o1.getPosition(), o2.getPosition());

	public static Direction getLayoutDirection(Direction farmSide) {
		return farmSide.getCounterClockWise();
	}

	/**
	 * @return the corner of the farm for the given side and layout. Returns null if the corner is not in a loaded chunk.
	 */
	private static BlockPos getFarmMultiblockCorner(BlockPos start, Direction farmSide, Direction layoutDirection, BlockPos minFarmCoord, BlockPos maxFarmCoord) {
		BlockPos edge = getFarmMultiblockEdge(start, farmSide, maxFarmCoord, minFarmCoord);
		return getFarmMultiblockEdge(edge, layoutDirection.getOpposite(), maxFarmCoord, minFarmCoord);
	}

	/**
	 * @return the edge of the farm for the given starting point and direction.
	 */
	private static BlockPos getFarmMultiblockEdge(BlockPos start, Direction direction, BlockPos maxFarmCoord, BlockPos minFarmCoord) {
		return switch (direction) {
			case NORTH -> // -z
				new BlockPos(start.getX(), start.getY(), minFarmCoord.getZ());
			case EAST -> // +x
				new BlockPos(maxFarmCoord.getX(), start.getY(), start.getZ());
			case SOUTH -> // +z
				new BlockPos(start.getX(), start.getY(), maxFarmCoord.getZ());
			case WEST -> // -x
				new BlockPos(minFarmCoord.getX(), start.getY(), start.getZ());
			default -> throw new IllegalArgumentException("Invalid farm direction: " + direction);
		};
	}

	public static void createTargets(Level world, IFarmHousing farmHousing, Map<Direction, List<FarmTarget>> targets, BlockPos targetStart, final int allowedExtent, final int farmSizeNorthSouth, final int farmSizeEastWest, BlockPos minFarmCoord, BlockPos maxFarmCoord) {
		for (Direction farmSide : HorizontalDirection.VALUES) {

			final int farmWidth;
			if (farmSide == Direction.NORTH || farmSide == Direction.SOUTH) {
				farmWidth = farmSizeEastWest;
			} else {
				farmWidth = farmSizeNorthSouth;
			}

			// targets extend sideways in a pinwheel pattern around the farm, so they need to go a little extra distance
			final int targetMaxLimit = allowedExtent + farmWidth;

			Direction layoutDirection = getLayoutDirection(farmSide);

			List<FarmTarget> farmSideTargets = new ArrayList<>();
			targets.put(farmSide, farmSideTargets);

			BlockPos targetLocation = FarmHelper.getFarmMultiblockCorner(targetStart, farmSide, layoutDirection, minFarmCoord, maxFarmCoord);
			BlockPos firstLocation = targetLocation.relative(farmSide);
			BlockPos firstGroundPosition = getGroundPosition(world, farmHousing, firstLocation);
			if (firstGroundPosition != null) {
				int groundHeight = firstGroundPosition.getY();

				for (int i = 0; i < allowedExtent; i++) {
					targetLocation = targetLocation.relative(farmSide);
					BlockPos groundLocation = new BlockPos(targetLocation.getX(), groundHeight, targetLocation.getZ());

					if (!world.hasChunkAt(groundLocation) || !farmHousing.isValidPlatform(world, groundLocation)) {
						break;
					}

					int targetLimit = targetMaxLimit;
					if (!farmHousing.isSquare()) {
						targetLimit = targetMaxLimit - i - 1;
					}

					FarmTarget target = new FarmTarget(targetLocation, layoutDirection, targetLimit);
					farmSideTargets.add(target);
				}
			}
		}
	}

	@Nullable
	private static BlockPos getGroundPosition(Level world, IFarmHousing farmHousing, BlockPos targetPosition) {
		if (!world.hasChunkAt(targetPosition)) {
			return null;
		}

		for (int yOffset = 2; yOffset > -4; yOffset--) {
			BlockPos position = targetPosition.offset(0, yOffset, 0);
			if (world.hasChunkAt(position) && farmHousing.isValidPlatform(world, position)) {
				return position;
			}
		}

		return null;
	}

	public static boolean isCycleCanceledByListeners(IFarmLogic logic, Direction direction, Iterable<IFarmListener> farmListeners) {
		for (IFarmListener listener : farmListeners) {
			if (listener.cancelTask(logic, direction)) {
				return true;
			}
		}
		return false;
	}

	public static void setExtents(Level world, IFarmHousing farmHousing, Map<Direction, List<FarmTarget>> targets) {
		for (List<FarmTarget> targetsList : targets.values()) {
			if (!targetsList.isEmpty()) {
				BlockPos groundPosition = getGroundPosition(world, farmHousing, targetsList.get(0).getStart());

				for (FarmTarget target : targetsList) {
					target.setExtentAndYOffset(world, groundPosition, farmHousing);
				}
			}
		}
	}

	public static boolean cultivateTarget(Level world, IFarmHousing farmHousing, FarmTarget target, IFarmLogic logic, Iterable<IFarmListener> farmListeners) {
		BlockPos targetPosition = target.getStart().offset(0, target.getYOffset(), 0);
		if (logic.cultivate(world, farmHousing, targetPosition, target.getDirection(), target.getExtent())) {
			for (IFarmListener listener : farmListeners) {
				listener.hasCultivated(logic, targetPosition, target.getDirection(), target.getExtent());
			}
			return true;
		}

		return false;
	}

	public static Collection<ICrop> harvestTargets(Level world, IFarmHousing housing, List<FarmTarget> farmTargets, IFarmLogic logic, Iterable<IFarmListener> farmListeners) {
		for (FarmTarget target : farmTargets) {
			Collection<ICrop> harvested = harvestTarget(world, housing, target, logic, farmListeners);
			if (!harvested.isEmpty()) {
				return harvested;
			}
		}

		return Collections.emptyList();
	}

	public static Collection<ICrop> harvestTarget(Level world, IFarmHousing housing, FarmTarget target, IFarmLogic logic, Iterable<IFarmListener> farmListeners) {
		BlockPos pos = target.getStart().offset(0, target.getYOffset(), 0);
		Collection<ICrop> harvested = logic.harvest(world, housing, target.getDirection(), target.getExtent(), pos);
		if (!harvested.isEmpty()) {
			// Let event handlers know.
			for (IFarmListener listener : farmListeners) {
				listener.hasScheduledHarvest(harvested, logic, pos, target.getDirection(), target.getExtent());
			}
		}
		return harvested;
	}
}
