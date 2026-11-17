package forestry.core.content.machines.inventory;

import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.platform.util.SlotUtil;
import forestry.core.content.machines.tiles.CentrifugeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class CentrifugeInventory extends BlockEntityInventoryAdapter<CentrifugeBlockEntity> {
	public static final int SLOT_RESOURCE = 0;
	public static final int SLOT_PRODUCT_1 = 1;
	public static final int SLOT_PRODUCT_COUNT = 9;

	public CentrifugeInventory(CentrifugeBlockEntity centrifuge) {
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
