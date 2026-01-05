package forestry.core.inventory;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.utils.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryPlain implements Container, INbtWritable, INbtReadable {

	private final NonNullList<ItemStack> contents;
	private final String name;
	private final int stackLimit;

	public InventoryPlain(int size, String name, int stackLimit) {
		this.contents = NonNullList.withSize(size, ItemStack.EMPTY);
		this.name = name;
		this.stackLimit = stackLimit;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.contents) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public NonNullList<ItemStack> getContents() {
		return this.contents;
	}

	@Override
	public int getContainerSize() {
		return this.contents.size();
	}

	@Override
	public ItemStack getItem(int slotId) {
		return this.contents.get(slotId);
	}

	@Override
	public ItemStack removeItem(int slotId, int count) {
		ItemStack itemStack = this.contents.get(slotId);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return itemStack.split(count);
	}

	@Override
	public void setItem(int slotId, ItemStack itemstack) {
        this.contents.set(slotId, itemstack);
	}

	@Override
	public int getMaxStackSize() {
		return this.stackLimit;
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player PlayerEntity) {
		return false;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slotIndex) {
		return this.getItem(slotIndex);
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemstack) {
		return true;
	}

	/* INBTagable */
	@Override
	public void read(CompoundTag CompoundNBT) {
		InventoryUtil.readFromNBT(this, this.name, CompoundNBT);
	}

	@Override
	public CompoundTag write(CompoundTag CompoundNBT) {
		InventoryUtil.writeToNBT(this, this.name, CompoundNBT);
		return CompoundNBT;
	}

	/* Fields */
	@Override
	public void clearContent() {
        this.contents.clear();
	}
}
