package forestry.core.items.definitions;

import net.minecraft.world.item.ItemStack;

/**
 * Allows an item to react to the usage of it in the fabricator machine.
 */
public interface ICraftingPlan {
	/**
	 * Called after an recipe was processed which involved this item as an plan.
	 * <p>
	 * The result will be used to replace the old stack of this plan in the inventory of the fabricator.
	 *
	 * @param plan   The stack that contains this item
	 * @param result The resulting stack of the recipe that this plan was used for
	 * @return A modified version of the given stack, an empty stack if the stack was used up
	 * or the given stack if nothing happened.
	 */
	ItemStack planUsed(ItemStack plan, ItemStack result);
}
