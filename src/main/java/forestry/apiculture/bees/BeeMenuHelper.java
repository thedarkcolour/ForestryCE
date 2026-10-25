package forestry.apiculture.bees;

import forestry.apiculture.apiary.ApiaryInventory;
import forestry.core.platform.gui.ForestryMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.api.core.IFilterSlotDelegate;
import net.minecraft.world.Container;

public abstract class BeeMenuHelper {
	public static <T extends Container & IFilterSlotDelegate> void addSlots(ForestryMenu container, T inventory, boolean hasFrames) {
		// Queen/Princess
		container.addSlot(new SlotFiltered(inventory, BeeHousingInventory.SLOT_QUEEN, 29, 39));

		// Drone
		container.addSlot(new SlotFiltered(inventory, BeeHousingInventory.SLOT_DRONE, 29, 65));

		// Frames
		if (hasFrames) {
			int slotFrames = ApiaryInventory.SLOT_FRAMES_1;
			container.addSlot(new SlotFiltered(inventory, slotFrames++, 66, 23).setStackLimit(1));
			container.addSlot(new SlotFiltered(inventory, slotFrames++, 66, 52).setStackLimit(1));
			container.addSlot(new SlotFiltered(inventory, slotFrames, 66, 81).setStackLimit(1));
		}

		// Product Inventory
		int slotProduct = BeeHousingInventory.SLOT_PRODUCT_1;
		container.addSlot(new SlotOutput(inventory, slotProduct++, 116, 52));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 137, 39));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 137, 65));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 116, 78));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 95, 65));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 95, 39));
		container.addSlot(new SlotOutput(inventory, slotProduct, 116, 26));
	}
}
