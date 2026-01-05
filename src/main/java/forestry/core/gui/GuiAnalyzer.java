package forestry.core.gui;

import forestry.core.config.Constants;
import forestry.core.gui.widgets.TankWidget;
import forestry.core.render.EnumTankLevel;
import forestry.core.tiles.TileAnalyzer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// The block form of the analyzer
public class GuiAnalyzer extends GuiForestryTitled<ContainerAnalyzer> {
	private final TileAnalyzer tile;

	public GuiAnalyzer(ContainerAnalyzer analyzer, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/alyzer.png", analyzer, inventory, title);
		this.tile = analyzer.tile;
		this.imageHeight = 176;
		this.widgetManager.add(new TankWidget(this.widgetManager, 95, 24, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);
		drawAnalyzeMeter(graphics, this.leftPos + 64, this.topPos + 30, this.tile.getProgressScaled(46), EnumTankLevel.rateTankLevel(this.tile.getProgressScaled(100)));
	}

	private void drawAnalyzeMeter(GuiGraphics graphics, int x, int y, int height, EnumTankLevel rated) {
		int i = 176 + rated.getLevelScaled(16);
		int k = 60;

		graphics.blit(this.textureFile, x, y + 46 - height, i, k + 46 - height, 4, height);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("analyzer");
	}
}
