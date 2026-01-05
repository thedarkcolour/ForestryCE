package forestry.factory.gui;

import forestry.core.gui.ContainerSocketed;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotLocked;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.network.packets.PacketItemStackDisplay;
import forestry.core.tiles.TileUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.tiles.TileCentrifuge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ContainerCentrifuge extends ContainerSocketed<TileCentrifuge> {
	private ItemStack oldCraftPreview = ItemStack.EMPTY;

	public static ContainerCentrifuge fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileCentrifuge tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileCentrifuge.class);
		return new ContainerCentrifuge(windowId, inv, tile);
	}

	public ContainerCentrifuge(int windowId, Inventory player, TileCentrifuge tile) {
		super(windowId, FactoryMenuTypes.CENTRIFUGE.menuType(), player, tile, 8, 84);

		// Resource
		this.addSlot(new SlotFiltered(this.tile, 0, 16, 37));

		// Craft Preview display
		this.addSlot(new SlotLocked(this.tile.getCraftPreviewInventory(), 0, 49, 37));

		// Product Inventory
		for (int l = 0; l < 3; l++) {
			for (int k = 0; k < 3; k++) {
				this.addSlot(new SlotOutput(this.tile, 1 + k + l * 3, 112 + k * 18, 19 + l * 18));
			}
		}
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		Container craftPreviewInventory = this.tile.getCraftPreviewInventory();

		ItemStack newCraftPreview = craftPreviewInventory.getItem(0);
		if (!ItemStack.matches(this.oldCraftPreview, newCraftPreview)) {
            this.oldCraftPreview = newCraftPreview;

			PacketItemStackDisplay packet = new PacketItemStackDisplay(this.tile, newCraftPreview);
			sendPacketToListeners(packet);
		}
	}
}
