package forestry.energy.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import forestry.core.gui.ContainerTile;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.TileUtil;
import forestry.energy.features.EnergyMenus;
import forestry.energy.tiles.PeatEngineBlockEntity;

public class PeatEngineMenu extends ContainerTile<PeatEngineBlockEntity> {
	public static PeatEngineMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf extraData) {
		PeatEngineBlockEntity tile = TileUtil.getTile(inv.player.level(), extraData.readBlockPos(), PeatEngineBlockEntity.class);
		return new PeatEngineMenu(windowId, inv, tile);
	}

	public PeatEngineMenu(int id, Inventory player, PeatEngineBlockEntity tile) {
		super(id, EnergyMenus.ENGINE_PEAT.menuType(), player, tile, 8, 84);

		addSlot(new SlotFiltered(tile, 0, 44, 46));

		addSlot(new SlotOutput(tile, 1, 98, 35));
		addSlot(new SlotOutput(tile, 2, 98, 53));
		addSlot(new SlotOutput(tile, 3, 116, 35));
		addSlot(new SlotOutput(tile, 4, 116, 53));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(tile);
		sendPacketToListeners(packet);
	}
}
