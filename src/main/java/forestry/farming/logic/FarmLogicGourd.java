package forestry.farming.logic;

import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FarmLogicGourd extends FarmLogicWatered {
	public FarmLogicGourd(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	protected boolean maintainCrops(Level world, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		for (int i = 0; i < extent; i++) {
			BlockPos position = translateWithOffset(pos, direction, i);
			if (!world.hasChunkAt(position)) {
				break;
			}

			BlockState state = world.getBlockState(position);
			if (!world.isEmptyBlock(position) && !BlockUtil.isReplaceableBlock(state, world, position)
				|| !isValidPosition(farmHousing, direction, position, CultivationType.CROP)) {
				continue;
			}

			BlockState groundState = world.getBlockState(position.below());
			if (isAcceptedSoil(groundState)) {
				return trySetCrop(world, farmHousing, position, direction);
			}
		}

		return false;
	}

	private boolean trySetCrop(Level world, IFarmHousing farmHousing, BlockPos position, Direction direction) {
		for (IFarmable candidate : getFarmables()) {
			if (farmHousing.plantGermling(candidate, world, position, direction)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean isValidPosition(IFarmHousing housing, Direction direction, BlockPos position, CultivationType type) {
		BlockPos farmLocation = housing.getFarmCorner(direction).relative(direction);
		int xVal = farmLocation.getX() & 1;
		int zVal = farmLocation.getZ() & 1;
		boolean uneven = ((position.getX() & 1) != xVal) ^ ((position.getZ() & 1) != zVal);
		return (type == CultivationType.WATER) != uneven;
	}
}
