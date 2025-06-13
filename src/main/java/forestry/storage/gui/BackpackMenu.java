package forestry.storage.gui;

import forestry.core.gui.ItemInventoryMenu;
import forestry.core.gui.slots.SlotFilteredInventory;
import forestry.storage.features.BackpackMenuTypes;
import forestry.storage.inventory.ItemInventoryBackpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BackpackMenu extends ItemInventoryMenu<ItemInventoryBackpack> {
	public record Size(int rows, int columns, int startX, int startY) {
		public static final Size DEFAULT = new Size(3, 5, 44, 19);
		public static final Size T2 = new Size(5, 9, 8, 8);

		public int getSize() {
			return this.rows * this.columns;
		}
	}

	private final Size size;

	public static BackpackMenu fromNetwork(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
		int slotIndex = extraData.readByte();
		Size size = extraData.readBoolean() ? Size.T2 : Size.DEFAULT;
		return new BackpackMenu(containerId, inv, size, slotIndex);
	}

	public BackpackMenu(int containerId, Inventory playerInv, Size size, int slotIndex) {
		super(BackpackMenuTypes.BACKPACK.menuType(), containerId, new ItemInventoryBackpack(size.getSize(), playerInv.getItem(slotIndex)), playerInv, 8, 11 + size.startY + size.rows * 18);
		this.size = size;
		// Inventory
		for (int j = 0; j < size.rows; j++) {
			for (int k = 0; k < size.columns; k++) {
				int slot = k + j * size.columns;
				addSlot(new SlotFilteredInventory(this.inventory, slot, size.startX + k * 18, size.startY + j * 18));
			}
		}
	}

	public Size getSize() {
		return this.size;
	}
}
