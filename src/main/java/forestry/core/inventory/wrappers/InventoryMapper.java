package forestry.core.inventory.wrappers;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Wrapper class used to specify part of an existing inventory to be treated as
 * a complete inventory. Used primarily to map a side of an ISidedInventory, but
 * it is also helpful for complex inventories such as the Tunnel Bore.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InventoryMapper extends InvWrapperBase implements Container {

	private final Container inv;
	private final int start;
	private final int size;
	private final int stackSizeLimit = -1;

	public InventoryMapper(Container inv) {
		this(inv, 0, inv.getContainerSize(), true);
	}

	public InventoryMapper(Container inv, boolean checkItems) {
		this(inv, 0, inv.getContainerSize(), checkItems);
	}

	/**
	 * Creates a new InventoryMapper
	 *
	 * @param inv   The backing inventory
	 * @param start The starting index
	 * @param size  The size of the new inventory, take care not to exceed the
	 *              end of the backing inventory
	 */
	public InventoryMapper(Container inv, int start, int size) {
		this(inv, start, size, true);
	}

	public InventoryMapper(Container inv, int start, int size, boolean checkItems) {
		super(inv, checkItems);
		this.inv = inv;
		this.start = start;
		this.size = size;
	}

	@Override
	public boolean isEmpty() {
		return this.inv.isEmpty();
	}

	@Override
	public int getContainerSize() {
		return this.size;
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.inv.getItem(this.start + slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return this.inv.removeItem(this.start + slot, amount);
	}

	@Override
	public void setItem(int slot, ItemStack itemstack) {
        this.inv.setItem(this.start + slot, itemstack);
	}

	@Override
	public int getMaxStackSize() {
		return this.stackSizeLimit > 0 ? this.stackSizeLimit : this.inv.getMaxStackSize();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return !checkItems() || this.inv.canPlaceItem(this.start + slot, stack);
	}

}
