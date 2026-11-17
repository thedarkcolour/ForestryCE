package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.slots.SlotEmptyLiquidContainerIn;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.FermenterInventory;
import forestry.core.content.machines.tiles.FermenterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class FermenterMenu extends LiquidTanksMenu<FermenterBlockEntity> {
	private final List<ContainerListener> trackedListeners = new ArrayList<>();

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

	public static FermenterMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		FermenterBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), FermenterBlockEntity.class);
		return new FermenterMenu(windowId, inv, tile);
	}

	public FermenterMenu(int windowId, Inventory player, FermenterBlockEntity tile) {
		super(windowId, FactoryMenuTypes.FERMENTER.menuType(), player, tile, 8, 84);
		addDataSlots(new SimpleContainerData(4));

		this.addSlot(new SlotFiltered(tile, FermenterInventory.SLOT_RESOURCE, 85, 23));
		this.addSlot(new SlotFiltered(tile, FermenterInventory.SLOT_FUEL, 75, 57));
		this.addSlot(new SlotOutput(tile, FermenterInventory.SLOT_CAN_OUTPUT, 150, 58));
		this.addSlot(new SlotEmptyLiquidContainerIn(tile, FermenterInventory.SLOT_CAN_INPUT, 150, 22));
		this.addSlot(new SlotLiquidIn(tile, FermenterInventory.SLOT_INPUT, 10, 40));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
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
	}
}
