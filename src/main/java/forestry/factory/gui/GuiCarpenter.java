package forestry.factory.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import forestry.factory.tiles.TileCarpenter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiCarpenter extends GuiForestryTitled<ContainerCarpenter> {
	private final TileCarpenter tile;

	public GuiCarpenter(ContainerCarpenter container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/carpenter.png", container, inventory, title);

		this.tile = container.getTile();
		this.imageHeight = 218;
		this.widgetManager.add(new TankWidget(this.widgetManager, 150, 17, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);
		int progressScaled = this.tile.getProgressScaled(16);
		graphics.blit(this.textureFile, this.leftPos + 98, this.topPos + 51 + 16 - progressScaled, 176, 60 + 16 - progressScaled, 4, progressScaled);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addPowerLedger(this.tile.getEnergyManager());
		addHintLedger("carpenter");
	}
}
