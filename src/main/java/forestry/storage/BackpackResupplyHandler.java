package forestry.storage;

import forestry.api.event.BackpackEvent;
import forestry.core.inventory.ItemInventory;
import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BackpackResupplyHandler {
	private static ArrayList<ItemStack> getBackpacks(Inventory playerInventory) {
		ArrayList<ItemStack> backpacks = new ArrayList<>();
		for (ItemStack itemStack : playerInventory.items) {
			if (itemStack.getItem() instanceof ItemBackpack) {
				backpacks.add(itemStack);
			}
		}
		return backpacks;
	}

	public static void resupply(Player player) {
		// Do not attempt resupplying if this backpack is already opened.
		if (player.containerMenu instanceof InventoryMenu) {
			for (ItemStack backpack : getBackpacks(player.getInventory())) {
				if (ItemBackpack.getMode(backpack) == BackpackMode.RESUPPLY) {
					// Load their inventory
					ItemBackpack backpackItem = (ItemBackpack) backpack.getItem();
					ItemInventory backpackInventory = new ItemInventoryBackpack(backpackItem.getBackpackSize(), backpack);

					BackpackEvent.Resupply event = new BackpackEvent.Resupply(player, backpackItem.getDefinition(), backpackInventory);
					if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
						for (int i = 0; i < backpackInventory.getContainerSize(); i++) {
							ItemStack itemStack = backpackInventory.getItem(i);
							if (topOffPlayerInventory(player, itemStack)) {
								backpackInventory.setItem(i, itemStack);
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This tops off existing stacks in the player's inventory.
	 * Adds to player inventory if there is an incomplete stack in there.
	 */
	private static boolean topOffPlayerInventory(Player player, ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			return false;
		}
		Inventory playerInventory = player.getInventory();
		List<ItemStack> inventory = new LinkedList<>();
		inventory.addAll(playerInventory.items);
		inventory.addAll(playerInventory.offhand);

		for (ItemStack inventoryStack : inventory) {
			if (playerInventory.hasRemainingSpaceForItem(inventoryStack, itemstack)) {
				inventoryStack.grow(1);
				inventoryStack.setPopTime(5);
				itemstack.shrink(1);
				return true;
			}
		}

		return false;
	}

}
