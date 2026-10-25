package forestry.core.platform.tile;

import forestry.core.platform.block.BaseBlock;
import forestry.core.platform.block.IBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseBlockEntity extends ForestryBlockEntity {
	public BaseBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		if (!hasGui()) {
			return;
		}
		player.openMenu(this, pos);
	}

	protected boolean hasGui() {
		return true;
	}

	public <T extends IBlockType> T getBlockType(T fallbackType) {
		BlockState blockState = getBlockState();
		Block block = blockState.getBlock();

		if (block instanceof BaseBlock<?> baseBlock) {
			return (T) baseBlock.blockType;
		} else {
			return fallbackType;
		}
	}
}
