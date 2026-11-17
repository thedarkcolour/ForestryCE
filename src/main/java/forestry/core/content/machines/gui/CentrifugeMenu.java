package forestry.core.content.machines.gui;

import forestry.core.platform.gui.SocketedMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotLocked;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.network.packets.PacketItemStackDisplay;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.tiles.CentrifugeBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CentrifugeMenu extends SocketedMenu<CentrifugeBlockEntity> {
	private ItemStack oldCraftPreview = ItemStack.EMPTY;

	public static CentrifugeMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		CentrifugeBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), CentrifugeBlockEntity.class);
		return new CentrifugeMenu(windowId, inv, tile);
	}

	public CentrifugeMenu(int windowId, Inventory player, CentrifugeBlockEntity tile) {
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
