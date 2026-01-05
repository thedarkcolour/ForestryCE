package forestry.farming.gui;

import forestry.core.gui.ContainerSocketed;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.TileUtil;
import forestry.farming.features.FarmingMenuTypes;
import forestry.farming.multiblock.InventoryFarm;
import forestry.farming.tiles.TileFarm;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.IFluidTank;

public class ContainerFarm extends ContainerSocketed<TileFarm> {
	public static ContainerFarm fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileFarm tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileFarm.class);
		return new ContainerFarm(windowId, inv, tile);
	}

	public ContainerFarm(int windowId, Inventory playerInventory, TileFarm data) {
		super(windowId, FarmingMenuTypes.FARM.menuType(), playerInventory, data, 28, 138);

		// Resources
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new SlotFiltered(this.tile, InventoryFarm.CONFIG.resourcesStart + j + i * 2, 123 + j * 18, 22 + i * 18));
			}
		}

		// Germlings
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new SlotFiltered(this.tile, InventoryFarm.CONFIG.germlingsStart + j + i * 2, 164 + j * 18, 22 + i * 18));
			}
		}

		// Production 1
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new SlotOutput(this.tile, InventoryFarm.CONFIG.productionStart + j + i * 2, 123 + j * 18, 86 + i * 18));
			}
		}

		// Production 2
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new SlotOutput(this.tile, InventoryFarm.CONFIG.productionStart + 4 + j + i * 2, 164 + j * 18, 86 + i * 18));
			}
		}

		// Fertilizer
		this.addSlot(new SlotFiltered(this.tile, InventoryFarm.CONFIG.fertilizerStart, 63, 95));
		// Can Slot
		this.addSlot(new SlotLiquidIn(this.tile, InventoryFarm.CONFIG.canStart, 15, 95));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(this.tile);
		sendPacketToListeners(packet);
	}

	public IFluidTank getTank(int slot) {
		return this.tile.getMultiblockLogic().getController().getTankManager().getTank(slot);
	}
}
