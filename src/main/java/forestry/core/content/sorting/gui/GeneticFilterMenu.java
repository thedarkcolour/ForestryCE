package forestry.core.content.sorting.gui;

import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.sorting.features.SortingMenuTypes;
import forestry.core.content.sorting.network.packets.PacketGuiFilterUpdate;
import forestry.core.content.sorting.tiles.GeneticFilterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class GeneticFilterMenu extends BlockEntityMenu<GeneticFilterBlockEntity> {
	private boolean guiNeedsUpdate = true;

	public static GeneticFilterMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		GeneticFilterBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), GeneticFilterBlockEntity.class);
		return new GeneticFilterMenu(windowId, inv, tile);
	}

	public GeneticFilterMenu(int windowId, Inventory playerInventory, GeneticFilterBlockEntity tile) {
		super(windowId, SortingMenuTypes.GENETIC_FILTER.menuType(), tile, playerInventory.player);
		addInventory(playerInventory, 26, 140);
	}

	protected void addInventory(Inventory playerInventory, int xInv, int yInv) {
		// Player inventory
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new SlotGeneticFilter(playerInventory, column + row * 9 + 9, xInv + column * 18, yInv + row * 18));
			}
		}
		// Player hotbar
		for (int column = 0; column < 9; column++) {
			addSlot(new SlotGeneticFilter(playerInventory, column, xInv + column * 18, yInv + 58));
		}

		for (int x = 0; x < 6; x++) {
			addSlot(new SlotFilterFacing(this.tile, x, 8, 18 + x * 18));
		}
	}

	public void setGuiNeedsUpdate(boolean guiNeedsUpdate) {
		this.guiNeedsUpdate = guiNeedsUpdate;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		if (this.guiNeedsUpdate) {
			PacketGuiFilterUpdate packet = this.tile.getLogic().createGuiUpdatePacket(this.tile.getBlockPos());
			sendPacketToListeners(packet);
            this.guiNeedsUpdate = false;
		}
	}

	public boolean hasSameTile(GeneticFilterMenu openContainer) {
		return this.tile == openContainer.tile;
	}
}
