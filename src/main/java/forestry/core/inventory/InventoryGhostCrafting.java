package forestry.core.inventory;

import net.minecraft.world.level.block.entity.BlockEntity;

public class InventoryGhostCrafting<T extends BlockEntity> extends InventoryAdapterTile<T> {
	public final static int SLOT_CRAFTING_1 = 0;
	public final static int SLOT_CRAFTING_COUNT = 9;
	public final static int SLOT_CRAFTING_RESULT = 9;

	public InventoryGhostCrafting(T tile, int size) {
		super(tile, size, "CraftItems");
	}
}
