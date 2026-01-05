package forestry.core.tiles;

import forestry.core.blocks.BlockBase;
import forestry.core.blocks.IBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

public abstract class TileBase extends TileForestry {
	public TileBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		if (!hasGui()) {
			return;
		}
		NetworkHooks.openScreen(player, this, pos);
	}

	protected boolean hasGui() {
		return true;
	}

	public <T extends IBlockType> T getBlockType(T fallbackType) {
		BlockState blockState = getBlockState();
		Block block = blockState.getBlock();

		if (block instanceof BlockBase<?> blockBase) {
			return (T) blockBase.blockType;
		} else {
			return fallbackType;
		}
	}
}
