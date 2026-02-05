package forestry.apiculture.items;

import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.core.utils.ItemTooltipUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class ItemScoop extends TieredItem {
	public ItemScoop() {
		super(Tiers.WOOD, new Item.Properties().durability(10));
	}

	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState state) {
		if (state.is(ForestryTags.Blocks.MINEABLE_SCOOP)) {
			return 2.0F;
		} else {
			return 1.0F;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity player) {
		stack.hurtAndBreak(2, player, (living) -> living.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState blockState, BlockPos pos, LivingEntity player) {
		if (!world.isClientSide && blockState.getDestroySpeed(world, pos) != 0.0F) {
			stack.hurtAndBreak(1, player, (living) -> living.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}

		return true;
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		Level level = interactionTarget.level();

		if (interactionTarget instanceof Bee) {
			if (!level.isClientSide()) {
				ItemEntity bee = new ItemEntity(level, interactionTarget.getX(), interactionTarget.getY(), interactionTarget.getZ(), SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.VANILLA, BeeLifeStage.DRONE));
				level.addFreshEntity(bee);
				level.playSound(null, interactionTarget.blockPosition(), SoundEvents.BEE_HURT, SoundSource.PLAYERS, 1f, 1f);
				interactionTarget.setRemoved(Entity.RemovalReason.DISCARDED);
				stack.hurtAndBreak(1, player, living -> living.broadcastBreakEvent(usedHand));
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantments.BLOCK_EFFICIENCY || enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.BLOCK_FORTUNE ||
			enchantment == Enchantments.MENDING || enchantment == Enchantments.VANISHING_CURSE || super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
		ItemTooltipUtil.addInformation(stack, world, tooltip, advanced);
	}
}
