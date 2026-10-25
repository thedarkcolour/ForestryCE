package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotWatched;
import forestry.core.platform.gui.slots.SlotWorking;
import forestry.core.platform.inventory.watchers.ISlotChangeWatcher;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.tiles.MoistenerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class MoistenerMenu extends LiquidTanksMenu<MoistenerBlockEntity> implements ISlotChangeWatcher {
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

	public static MoistenerMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		MoistenerBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), MoistenerBlockEntity.class);
		return new MoistenerMenu(windowId, inv, tile);
	}

	public MoistenerMenu(int windowId, Inventory player, MoistenerBlockEntity tile) {
		super(windowId, FactoryMenuTypes.MOISTENER.menuType(), player, tile, 8, 84);
		addDataSlots(new SimpleContainerData(4));

		// Stash
		for (int l = 0; l < 2; l++) {
			for (int k1 = 0; k1 < 3; k1++) {
				addSlot(new SlotFiltered(this.tile, k1 + l * 3, 39 + k1 * 18, 16 + l * 18));
			}
		}
		// Reservoir
		for (int k1 = 0; k1 < 3; k1++) {
			addSlot(new SlotFiltered(this.tile, k1 + 6, 39 + k1 * 18, 22 + 36));
		}

		// Working slot
		this.addSlot(new SlotWorking(this.tile, 9, 105, 37));

		// Product slot
		this.addSlot(new SlotFiltered(this.tile, 10, 143, 55));
		// Boxes
		this.addSlot(new SlotWatched(this.tile, 11, 143, 19).setChangeWatcher(this));
	}

	@Override
	public void onSlotChanged(Container inventory, int slot) {
        this.tile.setItem(slot, inventory.getItem(slot));
        this.tile.checkRecipe();
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
