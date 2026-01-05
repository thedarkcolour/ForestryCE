package forestry.mail.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import forestry.mail.tiles.TileStampCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiStampCollector extends GuiForestry<ContainerStampCollector> {
	private final TileStampCollector tile;

	public GuiStampCollector(ContainerStampCollector container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/philatelist.png", container, inv, title);
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
