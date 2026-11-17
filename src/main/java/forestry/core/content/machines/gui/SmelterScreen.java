package forestry.core.content.machines.gui;

import forestry.core.content.machines.tiles.SmelterBlockEntity;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import forestry.core.platform.gui.widgets.SocketWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SmelterScreen extends TitledForestryScreen<SmelterMenu> {
	private final SmelterBlockEntity tile;

	public SmelterScreen(SmelterMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/smelter.png", container, inventory, title);
		this.tile = container.getTile();
		this.widgetManager.add(new SocketWidget(this.widgetManager, 95, 21, this.tile, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		// Smelting progress bar
		int progress = this.tile.getProgressScaled(50);
		graphics.blit(this.textureFile,
			this.leftPos + 81, this.topPos + 39, // where to draw thing
			176, 52, // coordinates of thing to be drawn
			progress, 16); // width and height of thing to be drawn

		// Status indicator. Kinda redundant but it's a nice visual indicator
		if (!this.tile.getErrorLogic().hasErrors()) {
			graphics.blit(this.textureFile,
				this.leftPos + 96, this.topPos + 58,
				176, 68,
				14, 14);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("smelter");
		addPowerLedger(this.tile.getEnergyManager());
	}
}
