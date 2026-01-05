package forestry.core.inventory;

import forestry.core.config.Constants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum FakeInventoryAdapter implements IInventoryAdapter {
	INSTANCE;

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return false;
	}

	@Override
	public boolean isLocked(int slotIndex) {
		return false;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return Constants.SLOTS_NONE;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return false;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
		return false;
	}

	@Override
	public int getContainerSize() {
		return 0;
	}

	@Override
	public ItemStack getItem(int p_70301_1_) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int p_70298_1_, int p_70298_2_) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int p_70299_1_, ItemStack p_70299_2_) {
	}

	@Override
	public int getMaxStackSize() {
		return 0;
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}

	@Override
	public void startOpen(Player player) {
	}

	@Override
	public void stopOpen(Player player) {
	}

	@Override
	public boolean canPlaceItem(int p_94041_1_, ItemStack p_94041_2_) {
		return false;
	}

	@Override
	public void read(CompoundTag CompoundNBT) {
	}

	@Override
	public CompoundTag write(CompoundTag CompoundNBT) {
		return CompoundNBT;
	}

	@Override
	public void clearContent() {
	}
}
