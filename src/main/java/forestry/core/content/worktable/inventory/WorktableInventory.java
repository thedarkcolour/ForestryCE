package forestry.core.content.worktable.inventory;

import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.platform.util.SlotUtil;
import forestry.core.content.worktable.tiles.WorktableBlockEntity;
import net.minecraft.world.item.ItemStack;

public class WorktableInventory extends BlockEntityInventoryAdapter<WorktableBlockEntity> {
	public static final int SLOT_INVENTORY_1 = 0;
	public static final int SLOT_INVENTORY_COUNT = 18;

	public WorktableInventory(WorktableBlockEntity worktable) {
		super(worktable, 18, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_INVENTORY_1, SLOT_INVENTORY_COUNT);
	}
}
