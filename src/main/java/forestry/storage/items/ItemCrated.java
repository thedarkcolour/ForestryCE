package forestry.storage.items;

import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class ItemCrated extends ItemForestry implements IColoredItem {
	private final Supplier<ItemStack> contained;

	public ItemCrated(Supplier<ItemStack> contained) {
		this.contained = contained;
	}

	public ItemStack getContained() {
		return this.contained.get();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack heldItem = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			ItemStack contained = this.contained.get();
			if (contained.isEmpty() || heldItem.isEmpty()) {
				return InteractionResultHolder.pass(heldItem);
			}

			heldItem.shrink(1);

			ItemStack dropStack = contained.copy();
			dropStack.setCount(9);
			ItemStackUtil.dropItemStackAsEntity(dropStack, worldIn, playerIn.getX() - playerIn.getBbWidth() / 2f, playerIn.getY(), playerIn.getZ() - playerIn.getBbWidth() / 2f, 40);
		}
		return InteractionResultHolder.success(heldItem);
	}

	@Override
	public Component getName(ItemStack itemstack) {
		ItemStack contained = this.contained.get();
		if (contained.isEmpty()) {
			return Component.translatable("item.forestry.crate");
		} else {
			Component containedName = contained.getHoverName();
			return Component.translatable("for.item.crated.grammar", containedName);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		ItemColors colors = Minecraft.getInstance().getItemColors();
		ItemStack contained = this.contained.get();

		if (contained.isEmpty() || renderPass == 0) {
			return -1;
		}

		return colors.getColor(contained, renderPass - 1);
	}
}
