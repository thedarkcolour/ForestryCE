package forestry.apiculture.gui;

import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.inventory.InventoryAlvearySieve;
import forestry.apiculture.multiblock.TileAlvearySieve;
import forestry.core.gui.ContainerTile;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.inventory.watchers.ISlotPickupWatcher;
import forestry.core.tiles.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerAlvearySieve extends ContainerTile<TileAlvearySieve> {
	public static ContainerAlvearySieve fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileAlvearySieve tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileAlvearySieve.class);
		return new ContainerAlvearySieve(windowId, inv, tile);    //TODO nullability.
	}

	public ContainerAlvearySieve(int windowId, Inventory player, TileAlvearySieve tile) {
		super(windowId, ApicultureMenuTypes.ALVEARY_SIEVE.menuType(), player, tile, 8, 87);

		ISlotPickupWatcher crafter = tile.getCrafter();

		addSlot(new SlotOutput(tile, 0, 94, 52).setPickupWatcher(crafter));
		addSlot(new SlotOutput(tile, 1, 115, 39).setPickupWatcher(crafter));
		addSlot(new SlotOutput(tile, 2, 73, 39).setPickupWatcher(crafter));
		addSlot(new SlotOutput(tile, 3, 94, 26).setPickupWatcher(crafter));

		addSlot(new SlotFiltered(tile, InventoryAlvearySieve.SLOT_SIEVE, 43, 39).setPickupWatcher(crafter));
	}
}
