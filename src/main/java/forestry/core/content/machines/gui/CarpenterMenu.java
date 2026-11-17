package forestry.core.content.machines.gui;

import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.IContainerCrafting;
import forestry.core.platform.gui.slots.*;
import forestry.core.platform.network.packets.PacketItemStackDisplay;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.inventory.CarpenterInventory;
import forestry.core.content.machines.tiles.CarpenterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CarpenterMenu extends LiquidTanksMenu<CarpenterBlockEntity> implements IContainerCrafting {
	private ItemStack oldCraftPreview = ItemStack.EMPTY;

	public static CarpenterMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		CarpenterBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), CarpenterBlockEntity.class);
		return new CarpenterMenu(windowId, inv, tile);
	}

	public CarpenterMenu(int windowId, Inventory inventoryplayer, CarpenterBlockEntity tile) {
		super(windowId, FactoryMenuTypes.CARPENTER.menuType(), inventoryplayer, tile, 8, 136);

		// Internal inventory
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 9; k++) {
				addSlot(new Slot(tile, CarpenterInventory.SLOT_INVENTORY_1 + k + i * 9, 8 + k * 18, 90 + i * 18));
			}
		}

		// Liquid Input
		addSlot(new SlotLiquidIn(tile, CarpenterInventory.SLOT_CAN_INPUT, 120, 20));
		// Boxes
		addSlot(new SlotFiltered(tile, CarpenterInventory.SLOT_BOX, 83, 20));
		// Product
		addSlot(new SlotOutput(tile, CarpenterInventory.SLOT_PRODUCT, 120, 56));

		// Craft Preview display
		addSlot(new SlotLocked(tile.getCraftPreviewInventory(), 0, 80, 51));

		// Crafting matrix
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 3; k1++) {
				addSlot(new SlotCraftMatrix(this, tile.getCraftingInventory(), k1 + l * 3, 10 + k1 * 18, 20 + l * 18));
			}
		}
	}

	@Override
	public void onCraftMatrixChanged(Container iinventory, int slot) {
		this.tile.checkRecipe(this.tile.getLevel().registryAccess());
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

	public CarpenterBlockEntity getCarpenter() {
		return this.tile;
	}
}
