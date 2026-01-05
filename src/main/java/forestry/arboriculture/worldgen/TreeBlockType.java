package forestry.arboriculture.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class TreeBlockType implements ITreeBlockType {

	private final BlockState blockState;

	public TreeBlockType(BlockState blockState) {
		this.blockState = blockState;
	}

	@Override
	public void setDirection(Direction facing) {

	}

	@Override
	public boolean setBlock(LevelAccessor level, BlockPos pos) {
		return level.setBlock(pos, this.blockState, 18);
	}

	public BlockState getBlockState() {
		return this.blockState;
	}
}
