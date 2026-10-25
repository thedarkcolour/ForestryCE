package forestry.core.platform.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public interface IForestryTicker<T extends ForestryBlockEntity> extends BlockEntityTicker<T> {
	@Override
	default void tick(Level level, BlockPos pos, BlockState state, T tile) {
		tile.tickHelper.onTick();

		tick(tile, level, pos, state);
	}

	void tick(T tile, Level level, BlockPos pos, BlockState state);
}
