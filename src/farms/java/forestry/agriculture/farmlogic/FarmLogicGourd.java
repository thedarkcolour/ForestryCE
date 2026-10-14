package forestry.agriculture.farmlogic;

import forestry.api.agriculture.IFarmHousing;
import forestry.api.agriculture.IFarmType;
import forestry.api.agriculture.IFarmable;
import forestry.api.agriculture.Soil;
import forestry.core.platform.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
		// water only replaces a solid block, so the gaps between stems need soil too
		if (type == CultivationType.SOIL && !this.isManual) {
			return true;
		}
		return (type == CultivationType.WATER) != isStemPosition(housing, direction, position);
	}

	@Override
	protected BlockState getSoilState(IFarmHousing housing, Direction direction, BlockPos pos, Soil soil) {
		if (!isStemPosition(housing, direction, pos)) {
			// untilled, the gourds grow here
			Block untilled = Block.byItem(soil.resource().getItem());
			if (untilled != Blocks.AIR) {
				return untilled.defaultBlockState();
			}
		}
		return super.getSoilState(housing, direction, pos, soil);
	}

	@Override
	protected boolean isAcceptedSoil(IFarmHousing housing, Direction direction, BlockPos pos, BlockState state) {
		// accepts farmland just like StemBlock (see DefaultFarms.java IFarmType#addSoil)
		if (super.isAcceptedSoil(housing, direction, pos, state)) {
			return true;
		}
		return !isStemPosition(housing, direction, pos) && state.is(BlockTags.DIRT);
	}

	private static boolean isStemPosition(IFarmHousing housing, Direction direction, BlockPos position) {
		BlockPos farmLocation = housing.getFarmCorner(direction).relative(direction);
		int xVal = farmLocation.getX() & 1;
		int zVal = farmLocation.getZ() & 1;
		return ((position.getX() & 1) != xVal) ^ ((position.getZ() & 1) != zVal);
	}
}
