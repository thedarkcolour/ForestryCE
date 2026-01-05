package forestry.factory.gui;

import forestry.core.gui.ContainerLiquidTanks;
import forestry.core.gui.IContainerCrafting;
import forestry.core.gui.slots.*;
import forestry.core.network.packets.PacketItemStackDisplay;
import forestry.core.tiles.TileUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.inventory.InventoryCarpenter;
import forestry.factory.tiles.TileCarpenter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCarpenter extends ContainerLiquidTanks<TileCarpenter> implements IContainerCrafting {
	private ItemStack oldCraftPreview = ItemStack.EMPTY;

	public static ContainerCarpenter fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileCarpenter tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileCarpenter.class);
		return new ContainerCarpenter(windowId, inv, tile);
	}

	public ContainerCarpenter(int windowId, Inventory inventoryplayer, TileCarpenter tile) {
		super(windowId, FactoryMenuTypes.CARPENTER.menuType(), inventoryplayer, tile, 8, 136);

		// Internal inventory
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 9; k++) {
				addSlot(new Slot(tile, InventoryCarpenter.SLOT_INVENTORY_1 + k + i * 9, 8 + k * 18, 90 + i * 18));
			}
		}

		// Liquid Input
		addSlot(new SlotLiquidIn(tile, InventoryCarpenter.SLOT_CAN_INPUT, 120, 20));
		// Boxes
		addSlot(new SlotFiltered(tile, InventoryCarpenter.SLOT_BOX, 83, 20));
		// Product
		addSlot(new SlotOutput(tile, InventoryCarpenter.SLOT_PRODUCT, 120, 56));

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

	public TileCarpenter getCarpenter() {
		return this.tile;
	}
}
