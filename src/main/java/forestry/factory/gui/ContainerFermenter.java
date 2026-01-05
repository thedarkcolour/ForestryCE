package forestry.factory.gui;

import forestry.core.gui.ContainerLiquidTanks;
import forestry.core.gui.slots.SlotEmptyLiquidContainerIn;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.tiles.TileUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.inventory.InventoryFermenter;
import forestry.factory.tiles.TileFermenter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerFermenter extends ContainerLiquidTanks<TileFermenter> {
	public static ContainerFermenter fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileFermenter tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileFermenter.class);
		return new ContainerFermenter(windowId, inv, tile);
	}

	public ContainerFermenter(int windowId, Inventory player, TileFermenter tile) {
		super(windowId, FactoryMenuTypes.FERMENTER.menuType(), player, tile, 8, 84);
		addDataSlots(new SimpleContainerData(4));

		this.addSlot(new SlotFiltered(tile, InventoryFermenter.SLOT_RESOURCE, 85, 23));
		this.addSlot(new SlotFiltered(tile, InventoryFermenter.SLOT_FUEL, 75, 57));
		this.addSlot(new SlotOutput(tile, InventoryFermenter.SLOT_CAN_OUTPUT, 150, 58));
		this.addSlot(new SlotEmptyLiquidContainerIn(tile, InventoryFermenter.SLOT_CAN_INPUT, 150, 22));
		this.addSlot(new SlotLiquidIn(tile, InventoryFermenter.SLOT_INPUT, 10, 40));
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

		for (ContainerListener crafter : this.containerListeners) {
            this.tile.sendGUINetworkData(this, crafter);
		}
	}
}
