package forestry.core.content.analyzer;

import forestry.core.features.CoreMenuTypes;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.gui.slots.SlotWorking;
import forestry.core.platform.inventory.AnalyzerInventory;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import forestry.core.platform.gui.LiquidTanksMenu;

public class AnalyzerMenu extends LiquidTanksMenu<AnalyzerBlockEntity> {
	public static AnalyzerMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		AnalyzerBlockEntity analyzer = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), AnalyzerBlockEntity.class);
		return new AnalyzerMenu(windowId, playerInv, analyzer);
	}

	public AnalyzerMenu(int windowId, Inventory player, AnalyzerBlockEntity tile) {
		super(windowId, CoreMenuTypes.ANALYZER.menuType(), player, tile, 8, 94);

		// Input buffer
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 2; k++) {
				this.addSlot(new SlotFiltered(tile, AnalyzerInventory.SLOT_INPUT_1 + i * 2 + k, 8 + k * 18, 28 + i * 18));
			}
		}

		// Analyze slot
		this.addSlot(new SlotWorking(tile, AnalyzerInventory.SLOT_ANALYZE, 73, 59));

		// Can slot
		this.addSlot(new SlotLiquidIn(tile, AnalyzerInventory.SLOT_CAN, 143, 24));

		// Output buffer
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 2; k++) {
				this.addSlot(new SlotOutput(tile, AnalyzerInventory.SLOT_OUTPUT_1 + i * 2 + k, 134 + k * 18, 48 + i * 18));
			}
		}
	}
}
