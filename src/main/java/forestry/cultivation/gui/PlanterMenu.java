package forestry.cultivation.gui;

import forestry.core.gui.LiquidTanksMenu;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.TileUtil;
import forestry.cultivation.features.CultivationMenuTypes;
import forestry.cultivation.inventory.LegacyFarmInventory;
import forestry.cultivation.tiles.TilePlanter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class PlanterMenu extends LiquidTanksMenu<TilePlanter> {
	public static PlanterMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		TilePlanter planter = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), TilePlanter.class);
		return new PlanterMenu(windowId, playerInv, planter);
	}

	public PlanterMenu(int windowId, Inventory playerInventory, TilePlanter tileForestry) {
		super(windowId, CultivationMenuTypes.PLANTER.menuType(), playerInventory, tileForestry, 21, 110);

		// Resources
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotFiltered(this.tile.getInternalInventory(), LegacyFarmInventory.CONFIG.resourcesStart + j + i * 2, 11 + j * 18, 65 + i * 18));
			}
		}

		// Germlings
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotFiltered(this.tile.getInternalInventory(), LegacyFarmInventory.CONFIG.germlingsStart + j + i * 2, 71 + j * 18, 65 + i * 18));
			}
		}

		// Production
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotOutput(this.tile.getInternalInventory(), LegacyFarmInventory.CONFIG.productionStart + j + i * 2, 131 + j * 18, 65 + i * 18));
			}
		}

		// Fertilizer
		addSlot(new SlotFiltered(this.tile.getInternalInventory(), LegacyFarmInventory.CONFIG.fertilizerStart, 83, 22));
		// Can Slot
		addSlot(new SlotLiquidIn(this.tile.getInternalInventory(), LegacyFarmInventory.CONFIG.canStart, 178, 18));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(this.tile);
		sendPacketToListeners(packet);
	}
}
