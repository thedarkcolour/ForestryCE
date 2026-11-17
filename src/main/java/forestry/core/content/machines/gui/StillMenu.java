package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.slots.SlotEmptyLiquidContainerIn;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.StillInventory;
import forestry.core.content.machines.tiles.StillBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class StillMenu extends LiquidTanksMenu<StillBlockEntity> {
	public static StillMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		StillBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), StillBlockEntity.class);
		return new StillMenu(windowId, inv, tile);
	}

	public StillMenu(int windowId, Inventory player, StillBlockEntity tile) {
		super(windowId, FactoryMenuTypes.STILL.menuType(), player, tile, 8, 84);

		this.addSlot(new SlotOutput(tile, StillInventory.SLOT_PRODUCT, 150, 54));
		this.addSlot(new SlotEmptyLiquidContainerIn(tile, StillInventory.SLOT_RESOURCE, 150, 18));
		this.addSlot(new SlotLiquidIn(tile, StillInventory.SLOT_CAN, 10, 36));
	}
}
