package forestry.factory.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.SocketWidget;
import forestry.factory.tiles.TileCentrifuge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiCentrifuge extends GuiForestryTitled<ContainerCentrifuge> {
	private final TileCentrifuge tile;

	public GuiCentrifuge(ContainerCentrifuge container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/centrifugesocket2.png", container, inventory, title);
		this.tile = container.getTile();
        this.widgetManager.add(new SocketWidget(this.widgetManager, 79, 37, this.tile, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		int progress = this.tile.getProgressScaled(16);
		graphics.blit(this.textureFile, this.leftPos + 43, this.topPos + 36 + 17 - progress, 176, 17 - progress, 4, progress);
		graphics.blit(this.textureFile, this.leftPos + 67, this.topPos + 36 + 17 - progress, 176, 17 - progress, 4, progress);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("centrifuge");
		addPowerLedger(this.tile.getEnergyManager());
	}
}
