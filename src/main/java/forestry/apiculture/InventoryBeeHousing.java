package forestry.apiculture;

import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.genetics.ILifeStage;
import forestry.core.inventory.InventoryAdapterRestricted;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.SlotUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InventoryBeeHousing extends InventoryAdapterRestricted implements IBeeHousingInventory {
	public static final int SLOT_QUEEN = 0;
	public static final int SLOT_DRONE = 1;
	public static final int SLOT_PRODUCT_1 = 2;
	public static final int SLOT_PRODUCT_COUNT = 7;

	public InventoryBeeHousing(int size) {
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

	@Override
	public final boolean addProduct(ItemStack product, boolean all) {
		return InventoryUtil.tryAddStack(this, product, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT, all, true);
	}

}
