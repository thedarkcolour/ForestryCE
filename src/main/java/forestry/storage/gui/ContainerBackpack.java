package forestry.storage.gui;

import forestry.core.gui.ContainerItemInventory;
import forestry.core.gui.slots.SlotFilteredInventory;
import forestry.storage.features.BackpackMenuTypes;
import forestry.storage.inventory.ItemInventoryBackpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerBackpack extends ContainerItemInventory<ItemInventoryBackpack> {
	public record Size(int rows, int columns, int startX, int startY) {
		public static final Size DEFAULT = new Size(3, 5, 44, 19);
		public static final Size T2 = new Size(5, 9, 8, 8);

		public int getSize() {
			return this.rows * this.columns;
		}
	}

	private final Size size;

	public static ContainerBackpack fromNetwork(int windowID, Inventory inv, RegistryFriendlyByteBuf extraData) {
		Size size = extraData.readBoolean() ? Size.T2 : Size.DEFAULT;
		ItemStack parent = ItemStack.STREAM_CODEC.decode(extraData);
		return new ContainerBackpack(windowID, inv.player, size, parent);
	}

	public ContainerBackpack(int windowID, Player player, Size size, ItemStack parent) {
		super(windowID, new ItemInventoryBackpack(player, size.getSize(), parent), player.getInventory(), 8, 11 + size.startY + size.rows * 18, BackpackMenuTypes.BACKPACK.menuType());
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
