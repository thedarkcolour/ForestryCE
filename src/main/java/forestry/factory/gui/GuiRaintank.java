package forestry.factory.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import forestry.factory.tiles.TileRaintank;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiRaintank extends GuiForestryTitled<ContainerRaintank> {
	private final TileRaintank tile;

	//TODO these all store a tile. Make a superclass to automatically do it.
	public GuiRaintank(ContainerRaintank container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/raintank.png", container, inventory, title);
		this.tile = container.getTile();
        this.widgetManager.add(new TankWidget(this.widgetManager, 53, 17, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		if (this.tile.isFilling()) {
			int progress = this.tile.getFillProgressScaled(24);
			graphics.blit(this.textureFile, this.leftPos + 80, this.topPos + 39, 176, 74, progress, 16);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("raintank");
	}
}
