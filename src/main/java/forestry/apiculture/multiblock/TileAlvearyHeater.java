package forestry.apiculture.multiblock;

import forestry.apiculture.blocks.BlockAlveary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileAlvearyHeater extends TileAlvearyClimatiser {
	public TileAlvearyHeater(BlockPos pos, BlockState state) {
		super(BlockAlveary.Type.HEATER, pos, state, (byte) 1);
	}
}
