package forestry.core.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InventoryAdapterRestricted extends InventoryAdapter {
	public InventoryAdapterRestricted(int size, String name) {
		super(size, name);
	}

	public InventoryAdapterRestricted(int size, String name, int stackLimit) {
		super(size, name, stackLimit);
	}

	@Override
	public boolean canPlaceItem(int slotIndex, ItemStack itemStack) {
		return !itemStack.isEmpty() && canSlotAccept(slotIndex, itemStack);
	}

	@Override
	public final boolean canPlaceItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return !itemStack.isEmpty() && canPlaceItem(slotIndex, itemStack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return !itemStack.isEmpty();
	}
}
