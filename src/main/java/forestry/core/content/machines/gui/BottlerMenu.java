package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.slots.SlotEmptyLiquidContainerIn;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.BottlerInventory;
import forestry.core.content.machines.tiles.BottlerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BottlerMenu extends LiquidTanksMenu<BottlerBlockEntity> {
	public static BottlerMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		BottlerBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), BottlerBlockEntity.class);
		return new BottlerMenu(windowId, inv, tile);
	}

	public BottlerMenu(int windowId, Inventory player, BottlerBlockEntity tile) {
		super(windowId, FactoryMenuTypes.BOTTLER.menuType(), player, tile, 8, 84);

		this.addSlot(new SlotLiquidIn(tile, BottlerInventory.SLOT_INPUT_FULL_CONTAINER, 18, 7));
		this.addSlot(new SlotOutput(tile, BottlerInventory.SLOT_EMPTYING_PROCESSING, 18, 35).setPickupWatcher(tile));
		this.addSlot(new SlotOutput(tile, BottlerInventory.SLOT_OUTPUT_EMPTY_CONTAINER, 18, 63));
		this.addSlot(new SlotEmptyLiquidContainerIn(tile, BottlerInventory.SLOT_INPUT_EMPTY_CONTAINER, 142, 7));
		this.addSlot(new SlotOutput(tile, BottlerInventory.SLOT_FILLING_PROCESSING, 142, 35).setPickupWatcher(tile));
		this.addSlot(new SlotOutput(tile, BottlerInventory.SLOT_OUTPUT_FULL_CONTAINER, 142, 63));
	}
}
