package forestry.storage;

import forestry.api.storage.IBackpackDefinition;
import forestry.storage.gui.ContainerBackpack;
import forestry.storage.gui.ContainerNaturalistBackpack;
import forestry.storage.items.ItemBackpack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PickupHandlerStorage {
	public static boolean onItemPickup(Player player, ItemEntity entityitem) {
		ItemStack itemstack = entityitem.getItem();
		if (itemstack.isEmpty()) {
			return false;
		}

		// Do not pick up if a backpack is open
		if (player.containerMenu instanceof ContainerBackpack || player.containerMenu instanceof ContainerNaturalistBackpack) {
			return false;
		}

		// Make sure to top off manually placed itemstacks in player inventory first
		topOffPlayerInventory(player, itemstack);

		for (ItemStack pack : player.getInventory().items) {
			if (itemstack.isEmpty()) {
				break;
			}

			if (pack.isEmpty() || !(pack.getItem() instanceof ItemBackpack backpack)) {
				continue;
			}

			IBackpackDefinition backpackDefinition = backpack.getDefinition();
			if (backpackDefinition.getFilter().test(itemstack)) {
				ItemBackpack.tryStowing(player, pack, itemstack);
			}
		}

		return itemstack.isEmpty();
	}

	/**
	 * This tops off existing stacks in the player's inventory. That way you can keep f.e. a stack of dirt or cobblestone in your inventory which gets refreshed
	 * constantly by picked up items.
	 */
	private static void topOffPlayerInventory(Player player, ItemStack itemstack) {
		// Add to player inventory first, if there is an incomplete stack in
		// there.
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack inventoryStack = player.getInventory().getItem(i);
			// We only add to existing stacks.
			if (inventoryStack.isEmpty()) {
				continue;
			}

			// Already full
			if (inventoryStack.getCount() >= inventoryStack.getMaxStackSize()) {
				continue;
			}

			if (ItemStack.isSameItemSameComponents(inventoryStack, itemstack)) {
				int space = inventoryStack.getMaxStackSize() - inventoryStack.getCount();

				if (space > itemstack.getCount()) {
					// Enough space to add all
					inventoryStack.grow(itemstack.getCount());
					itemstack.setCount(0);
				} else {
					// Only part can be added
					inventoryStack.setCount(inventoryStack.getMaxStackSize());
					itemstack.shrink(space);
				}
			}
		}

	}

}
