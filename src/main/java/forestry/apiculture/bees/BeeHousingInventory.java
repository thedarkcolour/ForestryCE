package forestry.apiculture.bees;

import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.core.genetics.ILifeStage;
import forestry.core.platform.inventory.InventoryAdapterRestricted;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.SlotUtil;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class BeeHousingInventory extends InventoryAdapterRestricted implements IBeeHousingInventory {
	public static final int SLOT_QUEEN = 0;
	public static final int SLOT_DRONE = 1;
	public static final int SLOT_PRODUCT_1 = 2;
	public static final int SLOT_PRODUCT_COUNT = 7;

	public BeeHousingInventory(int size) {
		super(size, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		ILifeStage beeType = SpeciesUtil.BEE_TYPE.get().getLifeStage(stack);

		if (slotIndex == SLOT_QUEEN) {
			return beeType == BeeLifeStage.QUEEN || beeType == BeeLifeStage.PRINCESS;
		} else if (slotIndex == SLOT_DRONE) {
			return beeType == BeeLifeStage.DRONE;
		}
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		if (!super.canTakeItemThroughFace(slotIndex, itemstack, side)) {
			return false;
		}
		return SlotUtil.isSlotInRange(slotIndex, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT);
	}

	@Override
	public final ItemStack getQueen() {
		return getItem(SLOT_QUEEN);
	}

	@Override
	public final ItemStack getDrone() {
		return getItem(SLOT_DRONE);
	}

	@Override
	public final void setQueen(ItemStack stack) {
		setItem(SLOT_QUEEN, stack);
	}

	@Override
	public final void setDrone(ItemStack stack) {
		setItem(SLOT_DRONE, stack);
	}

	// the beekeeping logic writes through these without calling setChanged, and an idle housing never marks its
	// chunk from the tick (ex. a queen dying and her offspring moving into the inventory)
	@Override
	public void setItem(int slotId, ItemStack stack) {
		super.setItem(slotId, stack);
		setChanged();
	}

	@Override
	public ItemStack removeItem(int slotId, int count) {
		ItemStack removed = super.removeItem(slotId, count);
		if (!removed.isEmpty()) {
			setChanged();
		}
		return removed;
	}

	@Override
	public final boolean addProduct(ItemStack product, boolean all) {
		// grows existing stacks in place without going through setItem
		boolean added = InventoryUtil.tryAddStack(this, product, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT, all, true);
		if (added) {
			setChanged();
		}
		return added;
	}

}
