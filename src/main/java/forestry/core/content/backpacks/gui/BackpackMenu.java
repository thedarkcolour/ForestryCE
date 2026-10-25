package forestry.core.content.backpacks.gui;

import forestry.core.platform.gui.ItemInventoryMenu;
import forestry.core.platform.gui.slots.SlotFilteredInventory;
import forestry.core.content.backpacks.features.BackpackMenuTypes;
import forestry.core.content.backpacks.inventory.BackpackInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackMenu extends ItemInventoryMenu<BackpackInventory> {
	public enum Size {
		DEFAULT(3, 5, 44, 19),
		T2(5, 9, 8, 8);

		final int rows;
		final int columns;
		final int startX;
		final int startY;

		Size(int rows, int columns, int startX, int startY) {
			this.rows = rows;
			this.columns = columns;
			this.startX = startX;
			this.startY = startY;
		}

		public int getSize() {
			return this.rows * this.columns;
		}
	}

	private final Size size;

	public static BackpackMenu fromNetwork(int windowID, Inventory inv, RegistryFriendlyByteBuf buffer) {
		Size size = buffer.readEnum(Size.class);
		ItemStack parent = ItemStack.STREAM_CODEC.decode(buffer);
		return new BackpackMenu(windowID, inv.player, size, parent);
	}

	public BackpackMenu(int windowID, Player player, Size size, ItemStack parent) {
		super(windowID, new BackpackInventory(player, size.getSize(), parent), player.getInventory(), 8, 11 + size.startY + size.rows * 18, BackpackMenuTypes.BACKPACK.menuType());
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
