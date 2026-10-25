package forestry.core.content.energy.menu;

import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.energy.features.EnergyMenus;
import forestry.core.content.energy.tiles.SolarEngineBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class SolarEngineMenu extends BlockEntityMenu<SolarEngineBlockEntity> {
	public static SolarEngineMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf extraData) {
		SolarEngineBlockEntity tile = TileUtil.getTile(inv.player.level(), extraData.readBlockPos(), SolarEngineBlockEntity.class);
		return new SolarEngineMenu(windowId, inv, tile);
	}

	public SolarEngineMenu(int id, Inventory player, SolarEngineBlockEntity tile) {
		super(id, EnergyMenus.SOLAR_ENGINE.menuType(), player, tile, 8, 84);
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(this.tile);
		sendPacketToListeners(packet);
	}
}
