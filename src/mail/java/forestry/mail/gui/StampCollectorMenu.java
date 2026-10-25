package forestry.mail.gui;

import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.tile.TileUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.inventory.StampCollectorInventory;
import forestry.mail.postoffice.StampCollectorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class StampCollectorMenu extends BlockEntityMenu<StampCollectorBlockEntity> {
	public static StampCollectorMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		StampCollectorBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), StampCollectorBlockEntity.class);
		return new StampCollectorMenu(windowId, inv, tile);
	}

	public StampCollectorMenu(int windowId, Inventory inv, StampCollectorBlockEntity tile) {
		super(windowId, MailMenuTypes.STAMP_COLLECTOR.menuType(), inv, tile, 8, 111);

		// Filter
		addSlot(new SlotFiltered(tile, StampCollectorInventory.SLOT_FILTER, 80, 19));

		// Collected Stamps
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new SlotOutput(tile, j + i * 9 + StampCollectorInventory.SLOT_BUFFER_1, 8 + j * 18, 46 + i * 18));
			}
		}
	}
}
