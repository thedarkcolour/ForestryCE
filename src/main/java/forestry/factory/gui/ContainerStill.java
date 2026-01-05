package forestry.factory.gui;

import forestry.core.gui.ContainerLiquidTanks;
import forestry.core.gui.slots.SlotEmptyLiquidContainerIn;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.tiles.TileUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.inventory.InventoryStill;
import forestry.factory.tiles.TileStill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerStill extends ContainerLiquidTanks<TileStill> {
	public static ContainerStill fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileStill tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileStill.class);
		return new ContainerStill(windowId, inv, tile);
	}

	public ContainerStill(int windowId, Inventory player, TileStill tile) {
		super(windowId, FactoryMenuTypes.STILL.menuType(), player, tile, 8, 84);

		this.addSlot(new SlotOutput(tile, InventoryStill.SLOT_PRODUCT, 150, 54));
		this.addSlot(new SlotEmptyLiquidContainerIn(tile, InventoryStill.SLOT_RESOURCE, 150, 18));
		this.addSlot(new SlotLiquidIn(tile, InventoryStill.SLOT_CAN, 10, 36));
	}
}
