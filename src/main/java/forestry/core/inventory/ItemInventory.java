package forestry.core.inventory;

import forestry.core.tiles.IFilterSlotDelegate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.Arrays;

/**
 * An inventory that belongs to an item, like the Portable Analyzer, or a backpack.
 */
public abstract class ItemInventory implements Container, IFilterSlotDelegate {
	private static final String KEY_SLOTS = "Slots";

	private final IItemHandler itemHandler = new InvWrapper(this);
	private final ItemStack[] slots;
	private final int slotIndex;

	public ItemInventory(int size, int slotIndex) {
		this.slots = new ItemStack[size];
		this.slotIndex = slotIndex;

		Arrays.fill(slots, ItemStack.EMPTY);

		CompoundTag nbt = slotIndex.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
			slotIndex.setTag(nbt);
		}

		CompoundTag nbtSlots = nbt.getCompound(KEY_SLOTS);
		for (int i = 0; i < this.inventoryStacks.size(); i++) {
			String slotKey = getSlotNBTKey(i);
			if (nbtSlots.contains(slotKey)) {
				CompoundTag itemNbt = nbtSlots.getCompound(slotKey);
				ItemStack itemStack = ItemStack.parseOptional(registries, itemNbt);
				this.inventoryStacks.set(i, itemStack);
			} else {
				this.inventoryStacks.set(i, ItemStack.EMPTY);
			}
		}
	}

	public static int getOccupiedSlotCount(ItemStack itemStack) {
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) {
			return 0;
		}

		CompoundTag slotNbt = nbt.getCompound(KEY_SLOTS);
		return slotNbt.size();
	}

	protected ItemStack getParent() {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = this.player.getItemInHand(hand);
			if (isSameItemInventory(held, this.parent)) {
				return held;
			}
		}
		return this.parent;
	}

	protected void setParent(ItemStack parent) {
		this.parent = parent;
	}

	private static boolean isSameItemInventory(ItemStack base, ItemStack comparison) {
		if (base.isEmpty() || comparison.isEmpty()) {
			return false;
		}

		if (base.getItem() != comparison.getItem()) {
			return false;
		}

		CompoundTag baseTagCompound = base.getTag();
		CompoundTag comparisonTagCompound = comparison.getTag();
		if (baseTagCompound == null || comparisonTagCompound == null) {
			return false;
		}

		if (!baseTagCompound.contains(KEY_UID) || !comparisonTagCompound.contains(KEY_UID)) {
			return false;
		}

		int baseUID = baseTagCompound.getInt(KEY_UID);
		int comparisonUID = comparisonTagCompound.getInt(KEY_UID);
		return baseUID == comparisonUID;
	}

	private void writeToParentNBT() {
		ItemStack parent = getParent();

		CompoundTag nbt = parent.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
			parent.setTag(nbt);
		}

		CompoundTag slotsNbt = new CompoundTag();
		for (int i = 0; i < getContainerSize(); i++) {
			ItemStack itemStack = getItem(i);
			if (!itemStack.isEmpty()) {
				String slotKey = getSlotNBTKey(i);
				CompoundTag itemNbt = new CompoundTag();
				itemStack.save(itemNbt);
				slotsNbt.put(slotKey, itemNbt);
			}
		}

		nbt.put(KEY_SLOTS, slotsNbt);
		onWriteNBT(nbt);
	}

	private static String getSlotNBTKey(int i) {
		return Integer.toString(i, Character.MAX_RADIX);
	}

	protected void onWriteNBT(CompoundTag nbt) {
	}

	public void onSlotClick(int slotIndex, Player player) {
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventoryStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}


	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack itemstack = ContainerHelper.removeItem(this.inventoryStacks, index, count);

		if (!itemstack.isEmpty()) {
			this.setChanged();
		}

		return itemstack;
	}

	@Override
	public void setItem(int index, ItemStack itemstack) {
		this.inventoryStacks.set(index, itemstack);

		ItemStack parent = getParent();

		CompoundTag nbt = parent.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
			parent.setTag(nbt);
		}

		CompoundTag slotNbt;
		if (!nbt.contains(KEY_SLOTS)) {
			slotNbt = new CompoundTag();
			nbt.put(KEY_SLOTS, slotNbt);
		} else {
			slotNbt = nbt.getCompound(KEY_SLOTS);
		}

		String slotKey = getSlotNBTKey(index);

		if (itemstack.isEmpty()) {
			slotNbt.remove(slotKey);
		} else {
			CompoundTag itemNbt = new CompoundTag();
			itemstack.save(itemNbt);

			slotNbt.put(slotKey, itemNbt);
		}
	}

	@Override
	public ItemStack getItem(int i) {
		return this.inventoryStacks.get(i);
	}

	@Override
	public int getContainerSize() {
		return this.inventoryStacks.size();
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public final void setChanged() {
		writeToParentNBT();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean canPlaceItem(int slotIndex, ItemStack itemStack) {
		return canSlotAccept(slotIndex, itemStack);
	}

	@Override
	public void startOpen(Player player) {
	}

	@Override
	public void stopOpen(Player player) {
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack toReturn = getItem(slot);

		if (!toReturn.isEmpty()) {
			setItem(slot, ItemStack.EMPTY);
		}

		return toReturn;
	}

	/* IFilterSlotDelegate */
	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return true;
	}

	@Override
	public boolean isLocked(int slotIndex) {
		return false;
	}

	public IItemHandler getItemHandler() {
		return this.itemHandler;
	}

	@Override
	public void clearContent() {
		this.inventoryStacks.clear();
	}
}
