package forestry.factory.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import forestry.factory.tiles.TileMoistener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiMoistener extends GuiForestryTitled<ContainerMoistener> {
	private final TileMoistener tile;

	public GuiMoistener(ContainerMoistener container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/moistener.png", container, inventory, title);
		this.tile = container.getTile();
        this.widgetManager.add(new TankWidget(this.widgetManager, 16, 16, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		// Mycelium production progress
		if (this.tile.isProducing()) {
			int i1 = this.tile.getProductionProgressScaled(16);
			graphics.blit(this.textureFile, this.leftPos + 124, this.topPos + 36, 176, 74, 16 - i1, 16);
		}

		// Resource consumption progress
		if (this.tile.isWorking()) {
			int i1 = this.tile.getConsumptionProgressScaled(54);
			graphics.blit(this.textureFile, this.leftPos + 93, this.topPos + 18 + i1, 176, 92 + i1, 29, 54 - i1);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("moistener");
	}
}
