package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.IContainerCrafting;
import forestry.core.platform.gui.slots.SlotCraftMatrix;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.inventory.GhostCraftingInventory;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.FabricatorInventory;
import forestry.core.content.machines.tiles.FabricatorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class FabricatorMenu extends LiquidTanksMenu<FabricatorBlockEntity> implements IContainerCrafting {
	private final List<ContainerListener> trackedListeners = new ArrayList<>();
	private int previousHeatValue = 0;

	@Override
	public void addSlotListener(ContainerListener listener) {
		super.addSlotListener(listener);
		if (!this.trackedListeners.contains(listener)) {
			this.trackedListeners.add(listener);
		}
	}

	@Override
	public void removeSlotListener(ContainerListener listener) {
		super.removeSlotListener(listener);
		this.trackedListeners.remove(listener);
	}

	public static FabricatorMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		FabricatorBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), FabricatorBlockEntity.class);
		return new FabricatorMenu(windowId, inv, tile);
	}

	public FabricatorMenu(int windowId, Inventory playerInventory, FabricatorBlockEntity tile) {
		super(windowId, FactoryMenuTypes.FABRICATOR.menuType(), playerInventory, tile, 8, 129);
		addDataSlots(new SimpleContainerData(4));

		// Internal inventory
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(this.tile, FabricatorInventory.SLOT_INVENTORY_1 + k + i * 9, 8 + k * 18, 84 + i * 18));
			}
		}

		// Molten resource
		this.addSlot(new SlotFiltered(this.tile, FabricatorInventory.SLOT_METAL, 26, 21));

		// Plan
		this.addSlot(new SlotFiltered(this.tile, FabricatorInventory.SLOT_PLAN, 139, 17));

		// Result
		this.addSlot(new SlotOutput(this.tile, FabricatorInventory.SLOT_RESULT, 139, 53));

		// Crafting matrix
		for (int l = 0; l < 3; l++) {
			for (int k = 0; k < 3; k++) {
				this.addSlot(new SlotCraftMatrix(this, this.tile.getCraftingInventory(), GhostCraftingInventory.SLOT_CRAFTING_1 + k + l * 3, 67 + k * 18, 17 + l * 18));
			}
		}
	}

	@Override
	public void onCraftMatrixChanged(Container iinventory, int slot) {
	}

	@Override
	public void setData(int messageId, int data) {
		super.setData(messageId, data);

        this.tile.getGUINetworkData(messageId, data);
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		for (ContainerListener crafter : this.trackedListeners) {
            this.tile.sendGUINetworkData(this, crafter);
		}

		int currentHeat = this.tile.getHeat();
		if (currentHeat != this.previousHeatValue) {
			this.previousHeatValue = currentHeat;
			PacketGuiStream packet = new PacketGuiStream(this.tile);
			sendPacketToListeners(packet);
		}
	}
}
