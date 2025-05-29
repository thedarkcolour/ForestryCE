package forestry.apiculture.gui;

import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.multiblock.TileAlvearySwarmer;
import forestry.core.gui.ContainerTile;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.tiles.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerAlvearySwarmer extends ContainerTile<TileAlvearySwarmer> {
	public static ContainerAlvearySwarmer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileAlvearySwarmer tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileAlvearySwarmer.class);
		return new ContainerAlvearySwarmer(windowId, inv, tile);
	}

	public ContainerAlvearySwarmer(int windowId, Inventory player, TileAlvearySwarmer tile) {
		super(windowId, ApicultureMenuTypes.ALVEARY_SWARMER.menuType(), player, tile, 8, 87);

		this.addSlot(new SlotFiltered(tile, 0, 79, 52));
		this.addSlot(new SlotFiltered(tile, 1, 100, 39));
		this.addSlot(new SlotFiltered(tile, 2, 58, 39));
		this.addSlot(new SlotFiltered(tile, 3, 79, 26));
	}
}
