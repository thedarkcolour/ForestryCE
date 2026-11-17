package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksSocketedMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.SqueezerInventory;
import forestry.core.content.machines.tiles.SqueezerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class SqueezerMenu extends LiquidTanksSocketedMenu<SqueezerBlockEntity> {
	public static SqueezerMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		SqueezerBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), SqueezerBlockEntity.class);
		return new SqueezerMenu(windowId, inv, tile);
	}

	public SqueezerMenu(int windowId, Inventory player, SqueezerBlockEntity tile) {
		super(windowId, FactoryMenuTypes.SQUEEZER.menuType(), player, tile, 8, 84);

		// Resource inventory
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				this.addSlot(new SlotFiltered(this.tile, column + row * 3, 17 + column * 18, 21 + row * 18));
			}
		}

		// Remnants slot
		addSlot(new SlotOutput(this.tile, SqueezerInventory.SLOT_REMNANT, 97, 60));

		// Can slot
		addSlot(new SlotLiquidIn(this.tile, SqueezerInventory.SLOT_CAN_INPUT, 147, 24));
		// Output slot
		addSlot(new SlotOutput(this.tile, SqueezerInventory.SLOT_CAN_OUTPUT, 147, 60));
	}
}
