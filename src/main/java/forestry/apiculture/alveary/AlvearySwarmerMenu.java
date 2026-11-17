package forestry.apiculture.alveary;

import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.alveary.multiblock.AlvearySwarmerBlockEntity;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AlvearySwarmerMenu extends BlockEntityMenu<AlvearySwarmerBlockEntity> {
	public static AlvearySwarmerMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		AlvearySwarmerBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), AlvearySwarmerBlockEntity.class);
		return new AlvearySwarmerMenu(windowId, inv, tile);
	}

	public AlvearySwarmerMenu(int windowId, Inventory player, AlvearySwarmerBlockEntity tile) {
		super(windowId, ApicultureMenuTypes.ALVEARY_SWARMER.menuType(), player, tile, 8, 87);

		this.addSlot(new SlotFiltered(tile, 0, 79, 52));
		this.addSlot(new SlotFiltered(tile, 1, 100, 39));
		this.addSlot(new SlotFiltered(tile, 2, 58, 39));
		this.addSlot(new SlotFiltered(tile, 3, 79, 26));
	}
}
