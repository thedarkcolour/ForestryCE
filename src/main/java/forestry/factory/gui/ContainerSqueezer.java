package forestry.factory.gui;

import forestry.core.gui.ContainerLiquidTanksSocketed;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.tiles.TileUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.inventory.InventorySqueezer;
import forestry.factory.tiles.TileSqueezer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerSqueezer extends ContainerLiquidTanksSocketed<TileSqueezer> {
	public static ContainerSqueezer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileSqueezer tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileSqueezer.class);
		return new ContainerSqueezer(windowId, inv, tile);
	}

	public ContainerSqueezer(int windowId, Inventory player, TileSqueezer tile) {
		super(windowId, FactoryMenuTypes.SQUEEZER.menuType(), player, tile, 8, 84);

		// Resource inventory
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				this.addSlot(new SlotFiltered(this.tile, column + row * 3, 17 + column * 18, 21 + row * 18));
			}
		}

		// Remnants slot
		addSlot(new SlotOutput(this.tile, InventorySqueezer.SLOT_REMNANT, 97, 60));

		// Can slot
		addSlot(new SlotLiquidIn(this.tile, InventorySqueezer.SLOT_CAN_INPUT, 147, 24));
		// Output slot
		addSlot(new SlotOutput(this.tile, InventorySqueezer.SLOT_CAN_OUTPUT, 147, 60));
	}
}
