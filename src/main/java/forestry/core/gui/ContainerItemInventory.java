package forestry.core.gui;

import forestry.core.gui.slots.SlotLocked;
import forestry.core.inventory.ItemInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ContainerItemInventory<I extends ItemInventory> extends ContainerForestry {

	protected final I inventory;

	protected ContainerItemInventory(int windowId, I inventory, Inventory playerInventory, int xInv, int yInv, MenuType<?> type) {
		super(windowId, type, playerInventory.player);
		this.inventory = inventory;

		addPlayerInventory(playerInventory, xInv, yInv);
	}

	@Override
	protected void addHotbarSlot(Inventory playerInventory, int slot, int x, int y) {
		ItemStack stackInSlot = playerInventory.getItem(slot);

		if (this.inventory.isParentItemInventory(stackInSlot)) {
			addSlot(new SlotLocked(playerInventory, slot, x, y));
		} else {
			addSlot(new Slot(playerInventory, slot, x, y));
		}
	}

	@Override
	protected final boolean canAccess(Player player) {
		return stillValid(player);
	}

	@Override
	public final boolean stillValid(Player PlayerEntity) {
		return this.inventory.stillValid(PlayerEntity);
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickTypeIn, Player player) {
		super.clicked(slotId, button, clickTypeIn, player);

		if (slotId > 0) {
            this.inventory.onSlotClick(this.slots.get(slotId).getSlotIndex(), player);
		}
	}

	public I getItemInventory() {
		return this.inventory;
	}

}
