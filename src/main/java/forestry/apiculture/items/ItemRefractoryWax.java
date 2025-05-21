package forestry.apiculture.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import forestry.api.IForestryApi;
import forestry.core.items.ItemForestry;
import forestry.core.network.packets.PacketRefractoryWax;
import forestry.core.utils.NetworkUtil;

public class ItemRefractoryWax extends ItemForestry {
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Block waxed = IForestryApi.INSTANCE.getTreeManager().getRefractoryWaxed(state.getBlock());

		if (waxed != null) {
			BlockState waxedState = waxed.withPropertiesOf(state);
			Player player = context.getPlayer();
			ItemStack stack = context.getItemInHand();

			if (player instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
			}

			stack.shrink(1);
			level.setBlock(pos, waxedState, 11);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxedState));
			if (!level.isClientSide) {
				NetworkUtil.sendNetworkPacket(new PacketRefractoryWax(pos), pos, level);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return InteractionResult.PASS;
	}
}
