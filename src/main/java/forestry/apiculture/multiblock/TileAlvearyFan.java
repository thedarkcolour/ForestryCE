package forestry.apiculture.multiblock;

import forestry.apiculture.blocks.BlockAlveary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileAlvearyFan extends TileAlvearyClimatiser {
	public TileAlvearyFan(BlockPos pos, BlockState state) {
		super(BlockAlveary.Type.FAN, pos, state, (byte) -1);
	}
}
