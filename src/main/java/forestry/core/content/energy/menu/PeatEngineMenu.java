package forestry.core.content.energy.menu;

import forestry.core.platform.gui.SocketedMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.energy.features.EnergyMenus;
import forestry.core.content.energy.tiles.PeatEngineBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class PeatEngineMenu extends SocketedMenu<PeatEngineBlockEntity> {
	public static PeatEngineMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf extraData) {
		PeatEngineBlockEntity tile = TileUtil.getTile(inv.player.level(), extraData.readBlockPos(), PeatEngineBlockEntity.class);
		return new PeatEngineMenu(windowId, inv, tile);
	}

	public PeatEngineMenu(int id, Inventory player, PeatEngineBlockEntity tile) {
		super(id, EnergyMenus.PEAT_ENGINE.menuType(), player, tile, 8, 84);

		addSlot(new SlotFiltered(tile, 0, 44, 46));

		addSlot(new SlotOutput(tile, 1, 98, 35));
		addSlot(new SlotOutput(tile, 2, 98, 53));
		addSlot(new SlotOutput(tile, 3, 116, 35));
		addSlot(new SlotOutput(tile, 4, 116, 53));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(this.tile);
		sendPacketToListeners(packet);
	}
}
