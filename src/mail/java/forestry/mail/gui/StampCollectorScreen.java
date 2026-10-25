package forestry.mail.gui;

import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.ForestryScreen;
import forestry.mail.postoffice.StampCollectorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StampCollectorScreen extends ForestryScreen<StampCollectorMenu> {
	private final StampCollectorBlockEntity tile;

	public StampCollectorScreen(StampCollectorMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/stamp_collector.png", container, inv, title);
		this.tile = container.getTile();
		this.imageWidth = 176;
		this.imageHeight = 193;
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("philatelist");
	}
}
