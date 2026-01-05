package forestry.core.gui.slots;

import forestry.core.tiles.IFilterSlotDelegate;
import net.minecraft.world.Container;

/**
 * Useful for InventoryTweaks. Works like SlotFiltered but allows InventoryTweaks to sort it.
 */
public class SlotFilteredInventory extends SlotFiltered {
	public <T extends Container & IFilterSlotDelegate> SlotFilteredInventory(T inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
	}
}
