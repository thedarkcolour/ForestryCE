package forestry.factory.inventory;

import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.utils.RecipeUtils;
import forestry.core.utils.SlotUtil;
import forestry.factory.tiles.TileFabricator;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public class InventoryFabricator extends InventoryAdapterTile<TileFabricator> {
	public static final short SLOT_METAL = 0;
	public static final short SLOT_PLAN = 1;
	public static final short SLOT_RESULT = 2;
	public static final short SLOT_INVENTORY_1 = 3;
	public static final short SLOT_INVENTORY_COUNT = 18;
	public static final short SLOT_COUNT = 21;

	public InventoryFabricator(TileFabricator fabricator) {
		super(fabricator, SLOT_COUNT, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		RecipeManager recipeManager = this.tile.getLevel().getRecipeManager();

		if (slotIndex == SLOT_METAL) {
			return RecipeUtils.getFabricatorMeltingRecipe(recipeManager, stack) != null;
		} else if (slotIndex == SLOT_PLAN) {
			return RecipeUtils.isFabricatorPlan(recipeManager, stack);
		} else if (SlotUtil.isSlotInRange(slotIndex, SLOT_INVENTORY_1, SLOT_INVENTORY_COUNT)) {
			if (RecipeUtils.isFabricatorPlan(recipeManager, stack)) {
				return false;
			} else if (RecipeUtils.getFabricatorMeltingRecipe(recipeManager, stack) != null) {
				return false;
			}
		}
		return SlotUtil.isSlotInRange(slotIndex, SLOT_INVENTORY_1, SLOT_INVENTORY_COUNT);
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return slotIndex == SLOT_RESULT;
	}
}
