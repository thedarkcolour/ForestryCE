/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.farming.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.HorizontalDirection;
import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.api.farming.Soil;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.farmables.FarmableCocoa;

public class FarmLogicCocoa extends FarmLogicSoil {
	private static final int[] LAYOUT_POSITIONS = new int[]{4, 1, 3, 0, 2};
	private final IFarmable cocoa = new FarmableCocoa();

	public FarmLogicCocoa(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public boolean cultivate(Level level, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		if (maintainSoil(level, farmHousing, pos, direction, extent)) {
			return true;
		}
		BlockPos position = farmHousing.getValidPosition(direction, pos, extent, pos.above());
		boolean result = tryPlantingCocoa(level, farmHousing, position, direction);

		farmHousing.increaseExtent(direction, pos, extent);

		return result;
	}

	protected boolean maintainSoil(Level world, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		if (!farmHousing.canPlantSoil(isManual)) {
			return false;
		}
		BlockPos cornerPos = farmHousing.getFarmCorner(direction);
		int distance = getDistanceValue(direction.getClockWise(), cornerPos, pos) - 1;
		int layoutExtent = LAYOUT_POSITIONS[distance % LAYOUT_POSITIONS.length];
		for (Soil soil : getSoils()) {
			NonNullList<ItemStack> resources = NonNullList.create();
			resources.add(soil.resource());

			for (int i = 0; i < extent; i++) {
				BlockPos position = translateWithOffset(pos, direction, i);
				if (!world.hasChunkAt(position)) {
					break;
				}

				if (!isValidPosition(direction, position, pos, layoutExtent)
						|| !farmHousing.getFarmInventory().hasResources(resources)) {
					continue;
				}

				BlockPos platformPosition = position.below();
				if (!farmHousing.isValidPlatform(world, platformPosition)) {
					break;
				}

				for (int z = 0; z < 3; z++) {
					BlockPos location = position.above(z);

					BlockState state = world.getBlockState(location);
					if (z == 0 && !world.isEmptyBlock(location)
							|| z > 0 && isAcceptedSoil(state)
							|| !BlockUtil.isBreakableBlock(state, world, pos)) {
						continue;
					}

					if (!BlockUtil.isReplaceableBlock(state, world, location)) {
						BlockUtil.getBlockDrops(world, location).forEach(farmHousing::addPendingProduct);
						world.setBlockAndUpdate(location, Blocks.AIR.defaultBlockState());
						return trySetSoil(world, farmHousing, location, soil.resource(), soil.soilState());
					}

					if (!isManual) {
						return trySetSoil(world, farmHousing, location, soil.resource(), soil.soilState());
					}
				}
			}
		}

		return false;
	}

	protected int getDistanceValue(Direction facing, BlockPos posA, BlockPos posB) {
		BlockPos delta = posA.subtract(posB);
		int value = switch (facing.getAxis()) {
			case X -> delta.getX();
			case Y -> delta.getY();
			case Z -> delta.getZ();
		};
		return Math.abs(value);
	}

	//4, 1, 3, 0, 2
	protected boolean isValidPosition(Direction direction, BlockPos pos, BlockPos logicPos, int layoutExtent) {
		int distance = getDistanceValue(direction, pos, logicPos);
		return (distance % LAYOUT_POSITIONS.length) == (layoutExtent);
	}

	protected static boolean trySetSoil(Level world, IFarmHousing farmHousing, BlockPos position, ItemStack resource, BlockState ground) {
		NonNullList<ItemStack> resources = NonNullList.create();
		resources.add(resource);
		if (!farmHousing.getFarmInventory().hasResources(resources)) {
			return false;
		}
		if (!BlockUtil.setBlockWithPlaceSound(world, position, ground)) {
			return false;
		}
		farmHousing.getFarmInventory().removeResources(resources);
		return true;
	}

	@Override
	public Collection<ICrop> harvest(Level level, IFarmHousing housing, Direction direction, int extent, BlockPos pos) {
		BlockPos position = housing.getValidPosition(direction, pos, extent, pos.above());
		Collection<ICrop> crops = getHarvestBlocks(level, position);
		housing.increaseExtent(direction, pos, extent);

		return crops;
	}

	private boolean tryPlantingCocoa(Level world, IFarmHousing farmHousing, BlockPos position, Direction farmDirection) {
		BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
		BlockState blockState = world.getBlockState(current.set(position));
		while (isJungleTreeTrunk(blockState)) {
			for (Direction direction : HorizontalDirection.VALUES) {
				BlockPos candidate = new BlockPos(current.getX() + direction.getStepX(), current.getY(), current.getZ() + direction.getStepZ());
				if (world.hasChunkAt(candidate) && world.isEmptyBlock(candidate)) {
					return farmHousing.plantGermling(cocoa, world, candidate, farmDirection);
				}
			}

			current.move(Direction.UP);
			if (current.getY() - position.getY() > 1) {
				break;
			}

			blockState = world.getBlockState(current);
		}

		return false;
	}

	private static boolean isJungleTreeTrunk(BlockState state) {
		return state.is(BlockTags.JUNGLE_LOGS);
	}

	private Collection<ICrop> getHarvestBlocks(Level world, BlockPos position) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<ICrop> crops = new ArrayDeque<>();

		// Determine what type we want to harvest.
		BlockState blockState = world.getBlockState(position);

		ICrop crop = null;
		if (!blockState.is(BlockTags.LOGS)) {
			crop = cocoa.getCropAt(world, position, blockState);
			if (crop == null) {
				return crops;
			}
		}

		if (crop != null) {
			crops.add(crop);
		}

		List<BlockPos> candidates = processHarvestBlock(world, crops, seen, position, position);
		List<BlockPos> temp = new ArrayList<>();
		while (!candidates.isEmpty() && crops.size() < 20) {
			for (BlockPos candidate : candidates) {
				temp.addAll(processHarvestBlock(world, crops, seen, position, candidate));
			}
			candidates.clear();
			candidates.addAll(temp);
			temp.clear();
		}

		return crops;
	}

	private List<BlockPos> processHarvestBlock(Level world, ArrayDeque<ICrop> crops, Set<BlockPos> seen, BlockPos start, BlockPos position) {
		List<BlockPos> candidates = new ArrayList<>();

		// Get additional candidates to return
		for (int i = -1; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = -1; k < 2; k++) {
					BlockPos candidate = position.offset(i, j, k);
					if (candidate.equals(position)) {
						continue;
					}
					if (Math.abs(candidate.getX() - start.getX()) > 5) {
						continue;
					}
					if (Math.abs(candidate.getZ() - start.getZ()) > 5) {
						continue;
					}

					// See whether the given position has already been processed
					if (seen.contains(candidate)) {
						continue;
					}

					if (!world.hasChunkAt(candidate)) {
						continue;
					}

					BlockState blockState = world.getBlockState(candidate);
					ICrop crop = cocoa.getCropAt(world, candidate, blockState);
					if (crop != null) {
						crops.addFirst(crop);
						candidates.add(candidate);
						seen.add(candidate);
					} else if (blockState.is(BlockTags.LOGS)) {
						candidates.add(candidate);
						seen.add(candidate);
					}
				}
			}
		}

		return candidates;
	}

}
