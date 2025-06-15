package forestry.farming.tiles;

import forestry.api.farming.IFarmLogic;
import forestry.api.multiblock.IFarmComponent;
import forestry.farming.blocks.FarmBlock;
import forestry.farming.features.FarmingTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MultifarmControlBlockEntity extends AbstractMultifarmBlockEntity implements IFarmComponent.Listener {
	public MultifarmControlBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingTiles.CONTROL.tileType(), pos, state);
	}

	@Override
	public boolean cancelTask(IFarmLogic logic, Direction direction) {
		BlockPos pos = this.worldPosition;
		Level level = this.level;

		for (Direction facing : new Direction[]{Direction.UP, Direction.DOWN, direction}) {
			BlockState blockState = level.getBlockState(pos.relative(facing));

			if (!(blockState.getBlock() instanceof FarmBlock) && level.getSignal(pos, facing) > 0) {
				return true;
			}
		}
		return false;
	}
}
