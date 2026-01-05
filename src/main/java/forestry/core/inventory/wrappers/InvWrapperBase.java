package forestry.core.inventory.wrappers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Created by CovertJaguar on 3/6/2016 for Railcraft.
 */
public abstract class InvWrapperBase implements Container {

	private final Container inv;
	private boolean checkItems = true;

	public InvWrapperBase(Container inv) {
		this(inv, true);
	}

	public InvWrapperBase(Container inv, boolean checkItems) {
		this.inv = inv;
		this.checkItems = checkItems;
	}

	public Container getBaseInventory() {
		return this.inv;
	}

	@Override
	public int getContainerSize() {
		return this.inv.getContainerSize();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.inv.getItem(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return this.inv.removeItem(slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return this.inv.removeItemNoUpdate(slot);
	}

	@Override
	public void setItem(int slot, ItemStack itemstack) {
        this.inv.setItem(slot, itemstack);
	}

	@Override
	public int getMaxStackSize() {
		return this.inv.getMaxStackSize();
	}

	@Override
	public void setChanged() {
        this.inv.setChanged();
	}

	@Override
	public boolean stillValid(Player PlayerEntity) {
		return this.inv.stillValid(PlayerEntity);
	}

	@Override
	public void startOpen(Player player) {
        this.inv.startOpen(player);
	}

	@Override
	public void stopOpen(Player player) {
        this.inv.stopOpen(player);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return !this.checkItems || this.inv.canPlaceItem(slot, stack);
	}

	@Override
	public void clearContent() {
        this.inv.clearContent();
	}

	public boolean checkItems() {
		return this.checkItems;
	}
}

