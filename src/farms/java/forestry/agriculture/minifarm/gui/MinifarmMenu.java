package forestry.agriculture.minifarm.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.agriculture.features.FarmingMenuTypes;
import forestry.agriculture.minifarm.inventory.MinifarmInventory;
import forestry.agriculture.minifarm.tiles.AbstractMinifarmBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MinifarmMenu extends LiquidTanksMenu<AbstractMinifarmBlockEntity> {
	public static MinifarmMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		AbstractMinifarmBlockEntity planter = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), AbstractMinifarmBlockEntity.class);
		return new MinifarmMenu(windowId, playerInv, planter);
	}

	public MinifarmMenu(int windowId, Inventory playerInventory, AbstractMinifarmBlockEntity tileForestry) {
		super(windowId, FarmingMenuTypes.MINIFARM.menuType(), playerInventory, tileForestry, 21, 110);

		// Resources
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotFiltered(this.tile.getInternalInventory(), MinifarmInventory.CONFIG.resourcesStart + j + i * 2, 11 + j * 18, 65 + i * 18));
			}
		}

		// Germlings
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotFiltered(this.tile.getInternalInventory(), MinifarmInventory.CONFIG.germlingsStart + j + i * 2, 71 + j * 18, 65 + i * 18));
			}
		}

		// Production
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotOutput(this.tile.getInternalInventory(), MinifarmInventory.CONFIG.productionStart + j + i * 2, 131 + j * 18, 65 + i * 18));
			}
		}

		// Fertilizer
		addSlot(new SlotFiltered(this.tile.getInternalInventory(), MinifarmInventory.CONFIG.fertilizerStart, 83, 22));
		// Can Slot
		addSlot(new SlotLiquidIn(this.tile.getInternalInventory(), MinifarmInventory.CONFIG.canStart, 178, 18));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(this.tile);
		sendPacketToListeners(packet);
	}
}
