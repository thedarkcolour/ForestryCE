package forestry.core.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class ItemAssemblyKit extends ItemForestry {
	private final Supplier<ItemStack> assembled;

	public ItemAssemblyKit(Supplier<ItemStack> assembled) {
		super(new Item.Properties().stacksTo(24));
		this.assembled = assembled;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack heldItem = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			heldItem.shrink(1);
			ItemEntity entity = new ItemEntity(worldIn, playerIn.getX(), playerIn.getY(), playerIn.getZ(), this.assembled.get());
			worldIn.addFreshEntity(entity);
		}
		return InteractionResultHolder.success(heldItem);
	}
}
