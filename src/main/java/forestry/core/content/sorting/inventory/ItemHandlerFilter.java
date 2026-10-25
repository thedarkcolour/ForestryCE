package forestry.core.content.sorting.inventory;

import forestry.core.content.sorting.tiles.GeneticFilterBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class ItemHandlerFilter implements IItemHandler {
	private final GeneticFilterBlockEntity filter;
	private final IItemHandler itemHandler;
	private final Direction facing;

	public ItemHandlerFilter(GeneticFilterBlockEntity filter, Direction facing) {
		this.filter = filter;
		this.facing = facing;
		this.itemHandler = new InvWrapper(filter);
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack remaining = stack;
		for (Direction facing : this.filter.getValidDirections(stack, this.facing)) {
			remaining = this.itemHandler.insertItem(facing.get3DDataValue(), stack, simulate);
			if (remaining.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return remaining;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	//TODO
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.itemHandler.isItemValid(slot, stack);
	}
}
