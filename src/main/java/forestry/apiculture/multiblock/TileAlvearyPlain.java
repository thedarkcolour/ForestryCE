package forestry.apiculture.multiblock;

import forestry.apiculture.blocks.BlockAlvearyType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileAlvearyPlain extends TileAlveary {

	public TileAlvearyPlain(BlockPos pos, BlockState state) {
		super(BlockAlvearyType.PLAIN, pos, state);
	}

	@Override
	public boolean allowsAutomation() {
		return true;
	}
}
