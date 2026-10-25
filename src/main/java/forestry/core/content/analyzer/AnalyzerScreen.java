package forestry.core.content.analyzer;

import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.widgets.TankWidget;
import forestry.core.platform.render.EnumTankLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import forestry.core.platform.gui.TitledForestryScreen;

// The block form of the analyzer
public class AnalyzerScreen extends TitledForestryScreen<AnalyzerMenu> {
	private final AnalyzerBlockEntity tile;

	public AnalyzerScreen(AnalyzerMenu analyzer, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/alyzer.png", analyzer, inventory, title);
		this.tile = analyzer.getTile();
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
