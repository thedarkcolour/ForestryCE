package forestry.core.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWrench extends ItemForestry {
	public ItemWrench() {
		super(new Properties());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level worldIn = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.FAIL;
		}
		Direction facing = context.getClickedFace();
		InteractionHand hand = context.getHand();

		BlockState state = worldIn.getBlockState(pos);
		Block block = state.getBlock();
		BlockState rotatedState = block.rotate(state, worldIn, pos, Rotation.CLOCKWISE_90);
		if (rotatedState != state) {    //TODO - how to rotate based on a direction, might need helper method
			player.swing(hand);
			worldIn.setBlock(pos, rotatedState, Block.UPDATE_CLIENTS);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}
}
