package forestry.apiculture.alveary;

import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.alveary.multiblock.AlvearyHygroregulatorBlockEntity;
import forestry.core.platform.gui.LiquidTanksMenu;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AlvearyHygroregulatorMenu extends LiquidTanksMenu<AlvearyHygroregulatorBlockEntity> {
	public static AlvearyHygroregulatorMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		AlvearyHygroregulatorBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), AlvearyHygroregulatorBlockEntity.class);
		return new AlvearyHygroregulatorMenu(windowId, inv, tile);
	}

	public AlvearyHygroregulatorMenu(int windowId, Inventory playerInventory, AlvearyHygroregulatorBlockEntity tile) {
		super(windowId, ApicultureMenuTypes.ALVEARY_HYGROREGULATOR.menuType(), playerInventory, tile, 8, 84);

		addSlot(new SlotLiquidIn(tile, AlvearyHygroregulatorInventory.SLOT_INPUT, 56, 38));
	}
}
