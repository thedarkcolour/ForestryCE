package forestry.core.content.machines.gui;

import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.SmelterInventory;
import forestry.core.content.machines.tiles.SmelterBlockEntity;
import forestry.core.platform.gui.SocketedMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotLocked;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.network.packets.PacketItemStackDisplay;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SmelterMenu extends SocketedMenu<SmelterBlockEntity> {
	private ItemStack oldCraftPreview = ItemStack.EMPTY;

	public static SmelterMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		SmelterBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), SmelterBlockEntity.class);
		return new SmelterMenu(windowId, inv, tile);
	}

	public SmelterMenu(int windowId, Inventory player, SmelterBlockEntity tile) {
		super(windowId, FactoryMenuTypes.SMELTER.menuType(), player, tile, 8, 84);

		// Resources
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				this.addSlot(new SlotFiltered(this.tile,
					SmelterInventory.SLOT_INPUT_1 + y + x * 3, // slot index
					21 + x * 18, // x pos
					21 + y * 18)); // y pos
			}
		}

		// Craft Preview display
		this.addSlot(new SlotLocked(this.tile.getCraftPreviewInventory(), 0, 95, 39));

		// Product Inventory
		this.addSlot(new SlotOutput(this.tile, SmelterInventory.SLOT_OUTPUT, 139, 39));
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
