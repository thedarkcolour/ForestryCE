package forestry.apiculture.items;

import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.ICraftingPlan;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// todo figure out what this is supposed to be used for
public class ItemWaxCast extends ItemForestry implements ICraftingPlan {
	public ItemWaxCast() {
		super(new Item.Properties().durability(16).setNoRepair());
	}

	@Override
	public ItemStack planUsed(ItemStack plan, ItemStack result) {
		plan.setDamageValue(plan.getDamageValue() + result.getCount());
		if (plan.getDamageValue() >= plan.getMaxDamage()) {
			return ItemStack.EMPTY;
		} else {
			return plan;
		}
	}
}
