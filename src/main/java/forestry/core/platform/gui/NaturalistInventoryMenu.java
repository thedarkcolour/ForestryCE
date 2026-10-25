package forestry.core.platform.gui;

import forestry.api.core.IFilterSlotDelegate;
import forestry.api.core.genetics.ISpeciesType;
import forestry.core.features.CoreMenuTypes;
import forestry.core.platform.gui.slots.SlotFilteredInventory;
import forestry.core.platform.tile.NaturalistChestBlockEntity;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class NaturalistInventoryMenu extends BlockEntityMenu<NaturalistChestBlockEntity> implements IGuiSelectable, INaturalistMenu {
	public static final int COLUMNS = 8;
	public static final int VISIBLE_ROWS = 5;
	public static final int MAX_SCROLL = 11;
	private final SimpleContainerData scrollData = new SimpleContainerData(1);

	public NaturalistInventoryMenu(int windowId, Inventory player, NaturalistChestBlockEntity tile) {
		super(windowId, CoreMenuTypes.NATURALIST_INVENTORY.menuType(), player, tile, 7, 107);

		addDataSlots(this.scrollData);
		addScrollableInventory(this, tile, this.scrollData);
	}

	public static <T extends Container & IFilterSlotDelegate> void addScrollableInventory(ForestryMenu container, T inventory, SimpleContainerData scrollData) {
		ScrollingInventory<T> view = new ScrollingInventory<>(inventory, scrollData);
		for (int row = 0; row < VISIBLE_ROWS; row++) {
			for (int column = 0; column < COLUMNS; column++) {
				int slot = column + row * COLUMNS;
				container.addSlot(new SlotFilteredInventory(view, slot, 7 + column * 18, 7 + row * 18));
			}
		}
	}

	public static NaturalistInventoryMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		NaturalistChestBlockEntity tile = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), NaturalistChestBlockEntity.class);
		return new NaturalistInventoryMenu(windowId, playerInv, tile);
	}

	@Override
	public void handleSelectionRequest(ServerPlayer player, int primary, int secondary) {
		setScrollRow(primary);
	}

	public void setScrollRow(int row) {
		this.scrollData.set(0, Mth.clamp(row, 0, MAX_SCROLL));
	}

	public int getScrollRow() {
		return this.scrollData.get(0);
	}

	@Override
	public ISpeciesType<?, ?> getSpeciesType() {
		return this.tile.getSpeciesType();
	}

	@Override
	public void addSlotListener(ContainerListener listener) {
		super.addSlotListener(listener);

		// When a player opens a chest, they add a listener. The listener used to be the player itself, but now it's
		// a separate object that implements ContainerListener. Luckily, it's still declared as an anonymous class
		// inside of ServerPlayer, so we can identify it by its nest host. Hack fix for chests staying open :)
		if (listener.getClass().getNestHost() == ServerPlayer.class) {
			this.tile.increaseNumPlayersUsing();
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);

		if (player instanceof ServerPlayer) {
			this.tile.decreaseNumPlayersUsing();
		}
	}

	private static final class ScrollingInventory<T extends Container & IFilterSlotDelegate> implements Container, IFilterSlotDelegate {
		private final T inventory;
		private final SimpleContainerData scrollData;

		private ScrollingInventory(T inventory, SimpleContainerData scrollData) {
			this.inventory = inventory;
			this.scrollData = scrollData;
		}

		private int getInventorySlot(int slot) {
			return slot + this.scrollData.get(0) * COLUMNS;
		}

		private boolean isInventorySlot(int slot) {
			return getInventorySlot(slot) < this.inventory.getContainerSize();
		}

		@Override
		public int getContainerSize() {
			return COLUMNS * VISIBLE_ROWS;
		}

		@Override
		public boolean isEmpty() {
			for (int slot = 0; slot < getContainerSize(); slot++) {
				if (!getItem(slot).isEmpty()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public ItemStack getItem(int slot) {
			return isInventorySlot(slot) ? this.inventory.getItem(getInventorySlot(slot)) : ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItem(int slot, int amount) {
			return isInventorySlot(slot) ? this.inventory.removeItem(getInventorySlot(slot), amount) : ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return isInventorySlot(slot) ? this.inventory.removeItemNoUpdate(getInventorySlot(slot)) : ItemStack.EMPTY;
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			if (isInventorySlot(slot)) {
				this.inventory.setItem(getInventorySlot(slot), stack);
			}
		}

		@Override
		public void setChanged() {
			this.inventory.setChanged();
		}

		@Override
		public boolean stillValid(Player player) {
			return this.inventory.stillValid(player);
		}

		@Override
		public void clearContent() {
			for (int slot = 0; slot < getContainerSize(); slot++) {
				setItem(slot, ItemStack.EMPTY);
			}
		}

		@Override
		public boolean canSlotAccept(int slotIndex, ItemStack stack) {
			return isInventorySlot(slotIndex) && this.inventory.canSlotAccept(getInventorySlot(slotIndex), stack);
		}

		@Override
		public boolean isLocked(int slotIndex) {
			return !isInventorySlot(slotIndex) || this.inventory.isLocked(getInventorySlot(slotIndex));
		}
	}
}
