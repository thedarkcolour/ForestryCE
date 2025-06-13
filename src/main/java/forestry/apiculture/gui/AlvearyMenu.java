package forestry.apiculture.gui;

import forestry.api.modules.IForestryPacketClient;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.multiblock.TileAlveary;
import forestry.core.gui.TileMenu;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AlvearyMenu extends TileMenu<TileAlveary> {
	public static AlvearyMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileAlveary tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileAlveary.class);
		return new AlvearyMenu(windowId, inv, tile);
	}

	public AlvearyMenu(int windowid, Inventory playerInv, TileAlveary tile) {
		super(windowid, ApicultureMenuTypes.ALVEARY.menuType(), playerInv, tile, 8, 108);
		ContainerBeeHelper.addSlots(this, tile, false);

		tile.getBeekeepingLogic().clearCachedValues();
	}

	private int beeProgress = -1;

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int beeProgress = this.tile.getBeekeepingLogic().getBeeProgressPercent();
		if (this.beeProgress != beeProgress) {
			this.beeProgress = beeProgress;
			IForestryPacketClient packet = new PacketGuiStream(this.tile);
			sendPacketToListeners(packet);
		}
	}
}
