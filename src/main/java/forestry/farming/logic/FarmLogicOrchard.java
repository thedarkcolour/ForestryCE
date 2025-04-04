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

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.api.genetics.IFruitBearer;
import forestry.core.tiles.TileUtil;
import forestry.farming.logic.crops.CropFruit;

public class FarmLogicOrchard extends FarmLogic {
	public FarmLogicOrchard(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public Collection<ICrop> harvest(Level level, IFarmHousing housing, Direction direction, int extent, BlockPos pos) {
		BlockPos position = housing.getValidPosition(direction, pos, extent, pos.above());
		Collection<ICrop> crops = getHarvestBlocks(level, position);
		housing.increaseExtent(direction, pos, extent);

		return crops;
	}

	private Collection<ICrop> getHarvestBlocks(Level world, BlockPos position) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<ICrop> crops = new ArrayDeque<>();

		if (!world.hasChunkAt(position)) {
			return Collections.emptyList();
		}

		// Determine what type we want to harvest.
		BlockState state = world.getBlockState(position);
		if (!state.is(BlockTags.LOGS) && !isFruitBearer(world, position, state)) {
			return crops;
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

		// todo use MutableBlockPos
		// Get additional candidates to return
		for (int i = -2; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = -1; k < 2; k++) {
					BlockPos candidate = position.offset(i, j, k);
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
					if (!world.hasChunkAt(candidate) || world.isEmptyBlock(candidate)) {
						continue;
					}

					BlockState state = world.getBlockState(candidate);
					if (state.is(BlockTags.LOGS)) {
						candidates.add(candidate);
						seen.add(candidate);
					}
					if (isFruitBearer(world, candidate, state)) {
						candidates.add(candidate);
						seen.add(candidate);

						ICrop crop = getCropAt(world, candidate);
						if (crop != null) {
							crops.addFirst(crop);
						}
					}
				}
			}
		}

		return candidates;
	}

	private boolean isFruitBearer(Level world, BlockPos pos, BlockState state) {
		IFruitBearer tile = TileUtil.getTile(world, pos, IFruitBearer.class);
		if (tile != null) {
			return true;
		}

		for (IFarmable farmable : getFarmables()) {
			if (farmable.isSaplingAt(world, pos, state)) {
				return true;
			}
		}

		return false;
	}

	@Nullable
	private ICrop getCropAt(Level world, BlockPos position) {
		IFruitBearer fruitBearer = TileUtil.getTile(world, position, IFruitBearer.class);

		if (fruitBearer != null) {
			if (fruitBearer.hasFruit() && fruitBearer.getRipeness() >= 0.9f) {
				return new CropFruit(world, position);
			}
		} else {
			return getCrop(world, position);
		}
		return null;
	}
}
