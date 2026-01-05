package forestry.core.gui;

import forestry.core.features.CoreMenuTypes;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.gui.slots.SlotWorking;
import forestry.core.inventory.InventoryAnalyzer;
import forestry.core.tiles.TileAnalyzer;
import forestry.core.tiles.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerAnalyzer extends ContainerLiquidTanks<TileAnalyzer> {
	public static ContainerAnalyzer fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		TileAnalyzer analyzer = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), TileAnalyzer.class);
		return new ContainerAnalyzer(windowId, playerInv, analyzer);
	}

	public ContainerAnalyzer(int windowId, Inventory player, TileAnalyzer tile) {
		super(windowId, CoreMenuTypes.ANALYZER.menuType(), player, tile, 8, 94);

		// Input buffer
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 2; k++) {
				this.addSlot(new SlotFiltered(tile, InventoryAnalyzer.SLOT_INPUT_1 + i * 2 + k, 8 + k * 18, 28 + i * 18));
			}
		}

		// Analyze slot
		this.addSlot(new SlotWorking(tile, InventoryAnalyzer.SLOT_ANALYZE, 73, 59));

		// Can slot
		this.addSlot(new SlotLiquidIn(tile, InventoryAnalyzer.SLOT_CAN, 143, 24));

		// Output buffer
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 2; k++) {
				this.addSlot(new SlotOutput(tile, InventoryAnalyzer.SLOT_OUTPUT_1 + i * 2 + k, 134 + k * 18, 48 + i * 18));
			}
		}
	}
}
