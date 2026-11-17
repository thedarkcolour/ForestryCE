package forestry.core.platform.inventory;

import net.minecraft.world.level.block.entity.BlockEntity;

public class GhostCraftingInventory<T extends BlockEntity> extends BlockEntityInventoryAdapter<T> {
	public final static int SLOT_CRAFTING_1 = 0;
	public final static int SLOT_CRAFTING_COUNT = 9;
	public final static int SLOT_CRAFTING_RESULT = 9;

	public GhostCraftingInventory(T tile, int size) {
		super(tile, size, "CraftItems");
	}
}
