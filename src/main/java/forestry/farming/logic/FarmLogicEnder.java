package forestry.farming.logic;

import com.google.common.collect.ImmutableSet;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.crops.CropDestroy;
import forestry.farming.logic.farmables.FarmableChorus;

public class FarmLogicEnder extends FarmLogicHomogeneous {
	private static final Set<Direction> VALID_DIRECTIONS = ImmutableSet.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

	private final IFarmable chorusFarmable = FarmableChorus.INSTANCE;

	public FarmLogicEnder(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public List<ItemStack> collect(Level level, IFarmHousing farmHousing) {
		return collectEntityItems(level, farmHousing, true);
	}

	@Override
	public Collection<ICrop> harvest(Level level, IFarmHousing farmHousing, Direction direction, int extent, BlockPos pos) {
		BlockPos position = farmHousing.getValidPosition(direction, pos, extent, pos.above());
		Collection<ICrop> crops = harvestBlocks(level, position);
		farmHousing.increaseExtent(direction, pos, extent);

		return crops;
	}

	private Collection<ICrop> harvestBlocks(Level world, BlockPos position) {
		if (!world.hasChunkAt(position) || world.isEmptyBlock(position)) {
			return Collections.emptySet();
		}

		ICrop crop = getCrop(world, position);
		if (crop != null) {
			return Collections.singleton(crop);
		}

		ArrayDeque<ICrop> crops = new ArrayDeque<>();
		ArrayDeque<ICrop> plants = new ArrayDeque<>();
		harvestBlock(world, position, Direction.DOWN, plants, crops);
		//Remove all flowers before remove all plants
		if (!crops.isEmpty()) {
			return crops;
		}
		return plants;
	}

	private boolean harvestBlock(Level world, BlockPos pos, Direction from, ArrayDeque<ICrop> plants, ArrayDeque<ICrop> flowers) {
		BlockState blockState = world.getBlockState(pos);
		if (blockState.getBlock() == Blocks.CHORUS_FLOWER) {
			ICrop crop = chorusFarmable.getCropAt(world, pos, blockState);
			if (crop != null) {
				flowers.addFirst(crop);
				return false;
			}
			return false;
		} else if (blockState.getBlock() == Blocks.CHORUS_PLANT) {
			boolean canHarvest = true;
			for (Direction facing : VALID_DIRECTIONS) {
				if (facing == from) {
					continue;
				}
				canHarvest &= harvestBlock(world, pos.relative(facing), facing.getOpposite(), plants, flowers);
			}
			if (canHarvest) {
				plants.addFirst(new CropDestroy(world, Blocks.CHORUS_PLANT.defaultBlockState(), pos, null));
			}
			return canHarvest;
		}
		return true;
	}

	@Override
	protected boolean maintainSeedlings(Level world, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		for (int i = 0; i < extent; i++) {
			BlockPos position = translateWithOffset(pos, direction, i);
			BlockState state = world.getBlockState(position);
			if (!world.isEmptyBlock(position) && !BlockUtil.isReplaceableBlock(state, world, position)) {
				continue;
			}

			BlockPos soilPos = position.below();
			BlockState blockState = world.getBlockState(soilPos);
			if (!isAcceptedSoil(blockState)) {
				continue;
			}

			if (trySetCrop(world, farmHousing, position, direction)) {
				return true;
			}
		}

		return false;
	}
}
