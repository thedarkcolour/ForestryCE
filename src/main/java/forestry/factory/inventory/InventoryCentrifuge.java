package forestry.factory.inventory;

import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.utils.RecipeUtils;
import forestry.core.utils.SlotUtil;
import forestry.factory.tiles.TileCentrifuge;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InventoryCentrifuge extends InventoryAdapterTile<TileCentrifuge> {
	public static final int SLOT_RESOURCE = 0;
	public static final int SLOT_PRODUCT_1 = 1;
	public static final int SLOT_PRODUCT_COUNT = 9;

	public InventoryCentrifuge(TileCentrifuge centrifuge) {
		super(centrifuge, 10, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return slotIndex == SLOT_RESOURCE && RecipeUtils.getCentrifugeRecipe(this.tile.getLevel().getRecipeManager(), stack) != null;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT);
	}
}
