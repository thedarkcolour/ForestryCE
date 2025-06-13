package forestry.core.inventory;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.utils.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class InventoryPlain implements Container, INbtWritable, INbtReadable {
	private final ItemStack[] contents;
	private final String name;
	private final int stackLimit;

	public InventoryPlain(int size, String name, int stackLimit) {
		this.contents = new ItemStack[size];
		this.name = name;
		this.stackLimit = stackLimit;
		Arrays.fill(this.contents, ItemStack.EMPTY);
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

	@Override
	public int getContainerSize() {
		return this.contents.length;
	}

	@Override
	public ItemStack getItem(int slotId) {
		return this.contents[slotId];
	}

	@Override
	public ItemStack removeItem(int slotId, int count) {
		ItemStack itemStack = this.contents[slotId];
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return itemStack.split(count);
	}

	@Override
	public void setItem(int slotId, ItemStack stack) {
        this.contents[slotId] = stack;
	}

	@Override
	public int getMaxStackSize() {
		return this.stackLimit;
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slotIndex) {
		return this.getItem(slotIndex);
	}

	@Override
	public void read(CompoundTag nbt) {
		InventoryUtil.readFromNBT(this, this.name, nbt);
	}

	@Override
	public CompoundTag write(CompoundTag nbt) {
		InventoryUtil.writeToNBT(this, this.name, nbt);
		return nbt;
	}

	@Override
	public void clearContent() {
		Arrays.fill(this.contents, ItemStack.EMPTY);
	}
}
