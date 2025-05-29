package forestry.arboriculture.items;

import forestry.api.ForestryTags;
import forestry.api.arboriculture.IToolGrafter;
import forestry.core.items.ItemForestry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrafter extends ItemForestry implements IToolGrafter {
	public ItemGrafter(int maxDamage) {
		super(new Item.Properties().durability(maxDamage));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, ctx, tooltip, advanced);
		if (!stack.isDamaged()) {
			tooltip.add(Component.translatable("item.forestry.uses", stack.getMaxDamage() + 1).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return state.getBlock() instanceof LeavesBlock || state.is(BlockTags.LEAVES) || super.isCorrectToolForDrops(state);
	}

	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState state) {
		if (state.is(ForestryTags.Blocks.MINEABLE_GRAFTER)) {
			return 4.0f;
		} else {
			return 1.0f;
		}
	}

	// mostly copied from ShearsItem.mineBlock
	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
		if (!world.isClientSide && !state.is(BlockTags.FIRE)) {
			stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
		}
		return state.is(BlockTags.LEAVES);
	}

	@Override
	public float getSaplingModifier(ItemStack stack, Level world, Player player, BlockPos pos) {
		return 100f;
	}
}
