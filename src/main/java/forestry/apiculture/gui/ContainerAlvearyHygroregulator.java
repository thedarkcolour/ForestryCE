package forestry.apiculture.gui;

import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.inventory.InventoryHygroregulator;
import forestry.apiculture.multiblock.TileAlvearyHygroregulator;
import forestry.core.gui.ContainerLiquidTanks;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.tiles.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerAlvearyHygroregulator extends ContainerLiquidTanks<TileAlvearyHygroregulator> {
	public static ContainerAlvearyHygroregulator fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileAlvearyHygroregulator tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileAlvearyHygroregulator.class);
		return new ContainerAlvearyHygroregulator(windowId, inv, tile);
	}

	public ContainerAlvearyHygroregulator(int windowId, Inventory playerInventory, TileAlvearyHygroregulator tile) {
		super(windowId, ApicultureMenuTypes.ALVEARY_HYGROREGULATOR.menuType(), playerInventory, tile, 8, 84);

		addSlot(new SlotLiquidIn(tile, InventoryHygroregulator.SLOT_INPUT, 56, 38));
	}
}
