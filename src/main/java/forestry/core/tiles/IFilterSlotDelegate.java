package forestry.core.tiles;

import net.minecraft.world.item.ItemStack;

public interface IFilterSlotDelegate {
	/**
	 * Non-automation version of IInventory's isItemValidForSlot.
	 * Used to determine if a player can place a stack in a slot.
	 * <p>
	 * Combine this with Forestry's access permissions to implement isItemValidForSlot.
	 */
	boolean canSlotAccept(int slotIndex, ItemStack stack);

	/**
	 * Used to lock slots under special conditions.
	 * Locked slots will have an X over them.
	 */
	boolean isLocked(int slotIndex);
}
