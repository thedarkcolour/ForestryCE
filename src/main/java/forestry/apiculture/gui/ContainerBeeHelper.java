package forestry.apiculture.gui;

import forestry.apiculture.InventoryBeeHousing;
import forestry.apiculture.inventory.InventoryApiary;
import forestry.core.gui.ContainerForestry;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.tiles.IFilterSlotDelegate;
import net.minecraft.world.Container;

public abstract class ContainerBeeHelper {
	public static <T extends Container & IFilterSlotDelegate> void addSlots(ContainerForestry container, T inventory, boolean hasFrames) {
		// Queen/Princess
		container.addSlot(new SlotFiltered(inventory, InventoryBeeHousing.SLOT_QUEEN, 29, 39));

		// Drone
		container.addSlot(new SlotFiltered(inventory, InventoryBeeHousing.SLOT_DRONE, 29, 65));

		// Frames
		if (hasFrames) {
			int slotFrames = InventoryApiary.SLOT_FRAMES_1;
			container.addSlot(new SlotFiltered(inventory, slotFrames++, 66, 23).setStackLimit(1));
			container.addSlot(new SlotFiltered(inventory, slotFrames++, 66, 52).setStackLimit(1));
			container.addSlot(new SlotFiltered(inventory, slotFrames, 66, 81).setStackLimit(1));
		}

		// Product Inventory
		int slotProduct = InventoryBeeHousing.SLOT_PRODUCT_1;
		container.addSlot(new SlotOutput(inventory, slotProduct++, 116, 52));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 137, 39));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 137, 65));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 116, 78));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 95, 65));
		container.addSlot(new SlotOutput(inventory, slotProduct++, 95, 39));
		container.addSlot(new SlotOutput(inventory, slotProduct, 116, 26));
	}
}
