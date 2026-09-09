package forestry.core.content.energy.inventory;

import forestry.core.platform.inventory.InventoryAdapterTile;
import forestry.core.platform.util.SlotUtil;
import forestry.core.content.energy.tiles.PeatEngineBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import forestry.api.ForestryDataMaps;

public class InventoryEnginePeat extends InventoryAdapterTile<PeatEngineBlockEntity> {
	public static final short SLOT_FUEL = 0;
	public static final short SLOT_WASTE_1 = 1;
	public static final short SLOT_WASTE_COUNT = 4;

	public InventoryEnginePeat(PeatEngineBlockEntity engineCopper) {
		super(engineCopper, 5, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return slotIndex == SLOT_FUEL && stack.getItemHolder().getData(ForestryDataMaps.PEAT_FUELS) != null;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_WASTE_1, SLOT_WASTE_COUNT);
	}
}
