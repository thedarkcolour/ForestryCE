package forestry.energy.inventory;

import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.utils.RecipeUtils;
import forestry.core.utils.SlotUtil;
import forestry.energy.tiles.PeatEngineBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public class InventoryEnginePeat extends InventoryAdapterTile<PeatEngineBlockEntity> {
	public static final short SLOT_FUEL = 0;
	public static final short SLOT_WASTE_1 = 1;
	public static final short SLOT_WASTE_COUNT = 4;

	public InventoryEnginePeat(PeatEngineBlockEntity engineCopper) {
		super(engineCopper, 5, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex != SLOT_FUEL) {
			return false;
		}
		RecipeManager manager = RecipeUtils.getRecipeManager();
		return manager != null && RecipeUtils.getPeatFuel(manager, stack) != null;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_WASTE_1, SLOT_WASTE_COUNT);
	}
}
