package forestry.core.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;

public class ItemFertilizer extends ItemForestry {
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level worldIn = context.getLevel();
		InteractionHand hand = context.getHand();
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();
		if (player == null) {
			return InteractionResult.FAIL;
		}
		ItemStack heldItem = player.getItemInHand(hand);
		if (!player.mayUseItemAt(pos.relative(facing), facing, heldItem)) {
			return InteractionResult.FAIL;
		}

		if (BoneMealItem.applyBonemeal(heldItem, worldIn, pos, player)) {
			if (!worldIn.isClientSide) {
				worldIn.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}
