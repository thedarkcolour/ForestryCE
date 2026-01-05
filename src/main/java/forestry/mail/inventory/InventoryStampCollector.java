package forestry.mail.inventory;

import forestry.api.mail.IStamps;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.mail.tiles.TileStampCollector;
import net.minecraft.world.item.ItemStack;

public class InventoryStampCollector extends InventoryAdapterTile<TileStampCollector> {
	public static final short SLOT_FILTER = 0;
	public static final short SLOT_BUFFER_1 = 1;
	public static final short SLOT_BUFFER_COUNT = 27;

	public InventoryStampCollector(TileStampCollector tile) {
		super(tile, 28, "INV");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return stack.getItem() instanceof IStamps;
	}
}
