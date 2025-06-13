package forestry.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IColoredBlock {
	/**
	 * Called on the client to determine the tint color to use for each layer of this block's texture.
	 *
	 * @param state     The current block state.
	 * @param level     The level.
	 * @param pos       The position of the block.
	 * @param tintIndex The layer index.
	 * @return The 24-bit color multiplier to use when applying a tint. {@code 0xffffff} means no tint will be applied.
	 */
	int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
}
