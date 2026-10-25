package forestry.mail.gui;

import forestry.api.mail.IMailAddress;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.tile.TileUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.tradestation.TradeStationBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class TradeStationNamingMenu extends BlockEntityMenu<TradeStationBlockEntity> {
	public static TradeStationNamingMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TradeStationBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TradeStationBlockEntity.class);
		return new TradeStationNamingMenu(windowId, inv.player, tile);
	}

	public TradeStationNamingMenu(int windowId, Player player, TradeStationBlockEntity tile) {
		super(windowId, MailMenuTypes.TRADE_NAME.menuType(), tile, player);
	}

	public IMailAddress getAddress() {
		return this.tile.getAddress();
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (this.tile.isLinked() && this.player != null) {
            this.tile.openGui(this.player, InteractionHand.MAIN_HAND, this.tile.getBlockPos());
		}
	}
}
