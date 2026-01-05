package forestry.apiculture.multiblock;

import forestry.apiculture.blocks.BlockAlvearyType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileAlvearyHeater extends TileAlvearyClimatiser {
	public TileAlvearyHeater(BlockPos pos, BlockState state) {
		super(BlockAlvearyType.HEATER, pos, state, (byte) 1);
	}
}
