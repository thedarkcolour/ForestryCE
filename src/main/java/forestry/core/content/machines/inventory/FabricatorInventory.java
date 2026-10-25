package forestry.core.content.machines.inventory;

import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.platform.util.SlotUtil;
import forestry.core.content.machines.tiles.FabricatorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public class FabricatorInventory extends BlockEntityInventoryAdapter<FabricatorBlockEntity> {
	public static final short SLOT_METAL = 0;
	public static final short SLOT_PLAN = 1;
	public static final short SLOT_RESULT = 2;
	public static final short SLOT_INVENTORY_1 = 3;
	public static final short SLOT_INVENTORY_COUNT = 18;
	public static final short SLOT_COUNT = 21;

	public FabricatorInventory(FabricatorBlockEntity fabricator) {
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
