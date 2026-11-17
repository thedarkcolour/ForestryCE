package forestry.apiculture.alveary;

import forestry.apiculture.alveary.multiblock.AlvearySieveBlockEntity;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.inventory.watchers.ISlotPickupWatcher;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AlvearySieveMenu extends BlockEntityMenu<AlvearySieveBlockEntity> {
	public static AlvearySieveMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		AlvearySieveBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), AlvearySieveBlockEntity.class);
		return new AlvearySieveMenu(windowId, inv, tile);
	}

	public AlvearySieveMenu(int windowId, Inventory player, AlvearySieveBlockEntity tile) {
		super(windowId, ApicultureMenuTypes.ALVEARY_SIEVE.menuType(), player, tile, 8, 87);

		ISlotPickupWatcher crafter = tile.getCrafter();

		addSlot(new SlotOutput(tile, 0, 94, 52).setPickupWatcher(crafter));
		addSlot(new SlotOutput(tile, 1, 115, 39).setPickupWatcher(crafter));
		addSlot(new SlotOutput(tile, 2, 73, 39).setPickupWatcher(crafter));
		addSlot(new SlotOutput(tile, 3, 94, 26).setPickupWatcher(crafter));

		addSlot(new SlotFiltered(tile, AlvearySieveInventory.SLOT_SIEVE, 43, 39).setPickupWatcher(crafter));
	}
}
