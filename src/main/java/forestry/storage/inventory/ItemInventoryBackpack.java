package forestry.storage.inventory;

import forestry.api.storage.BackpackDefinition;
import forestry.core.inventory.ItemInventory;
import forestry.storage.items.ItemBackpack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInventoryBackpack extends ItemInventory {
	private final BackpackDefinition backpackDefinition;

	public ItemInventoryBackpack(int size, ItemStack parent) {
		super(size, parent);

		Item item = parent.getItem();

		this.backpackDefinition = ((ItemBackpack) item).getDefinition();
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return this.backpackDefinition.filter().test(stack);
	}
}
