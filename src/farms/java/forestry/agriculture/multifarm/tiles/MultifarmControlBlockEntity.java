package forestry.agriculture.multifarm.tiles;

import forestry.agriculture.features.FarmingBlockEntities;
import forestry.api.agriculture.IFarmListener;
import forestry.api.agriculture.IFarmLogic;
import forestry.api.core.multiblock.IFarmComponent;
import forestry.agriculture.multifarm.blocks.MultifarmBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MultifarmControlBlockEntity extends MultifarmBlockEntity implements IFarmComponent.Listener {
	private final IFarmListener farmListener;

	public MultifarmControlBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.CONTROL.tileType(), pos, state);
		this.farmListener = new ControlFarmListener(this);
	}

	@Override
	public IFarmListener getFarmListener() {
		return this.farmListener;
	}

	private static class ControlFarmListener implements IFarmListener {
		private final MultifarmControlBlockEntity tile;

		public ControlFarmListener(MultifarmControlBlockEntity tile) {
			this.tile = tile;
		}

		@Override
		public boolean cancelTask(IFarmLogic logic, Direction direction) {
			for (Direction facing : new Direction[]{Direction.UP, Direction.DOWN, direction}) {
				BlockPos pos = this.tile.getBlockPos();
				Level world = this.tile.getLevel();
				BlockState blockState = world.getBlockState(pos.relative(facing));
				if (!(blockState.getBlock() instanceof MultifarmBlock) && world.getSignal(pos, facing) > 0) {
					return true;
				}
			}
			return false;
		}
	}

}
