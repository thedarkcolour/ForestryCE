package forestry.core.inventory;

import net.minecraft.world.item.ItemStack;

/**
 * This Interface represents an abstract inventory slot. It provides a unified interface for interfacing with Inventories.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public interface IInvSlot {

	boolean canPutStackInSlot(ItemStack stack);

	boolean canTakeStackFromSlot(ItemStack stack);

	ItemStack decreaseStackInSlot();

	/**
	 * It is not legal to edit the stack returned from this function.
	 */
	ItemStack getStackInSlot();

	//    void setStackInSlot(ItemStack stack);

	int getIndex();

}
