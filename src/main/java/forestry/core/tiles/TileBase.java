package forestry.core.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import forestry.core.blocks.BlockBase;
import forestry.core.blocks.IBlockType;

public abstract class TileBase extends TileForestry {
	public TileBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public boolean interactWithItem(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		return false;
	}

	public boolean interactNoItem(Level level, Player player, BlockPos pos) {
		player.openMenu(this, pos);
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
