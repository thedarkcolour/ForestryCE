package forestry.core.content.machines.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import forestry.core.platform.gui.widgets.TankWidget;
import forestry.core.platform.render.ColourProperties;
import forestry.core.content.machines.tiles.BottlerBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BottlerScreen extends TitledForestryScreen<BottlerMenu> {
	private final BottlerBlockEntity tile;

	public BottlerScreen(BottlerMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/bottler.png", container, inventory, title);
		this.tile = container.getTile();
        this.widgetManager.add(new TankWidget(this.widgetManager, 80, 14, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		bindTexture(this.textureFile);

		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.blit(this.textureFile, x, y, 0, 0, this.imageWidth, this.imageHeight);

		//RenderHelper.enableGUIStandardItemLighting(); TODO Gui Light
		// RenderSystem.disableLighting();
		// RenderSystem.enableRescaleNormal();
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		PoseStack pose = graphics.pose();
		pose.pushPose();
		{
			pose.translate(this.leftPos, this.topPos, 0.0F);
			drawWidgets(graphics);
		}
		pose.popPose();

		Component name = this.tile.getTitle();
        this.textLayout.line = 5;
        this.textLayout.drawCenteredLine(graphics, name, 0, ColourProperties.INSTANCE.get("gui.title"));
		bindTexture(this.textureFile);

		bindTexture(this.textureFile);

		BottlerBlockEntity bottler = this.tile;
		int progressArrow = bottler.getProgressScaled(22);
		if (progressArrow > 0) {
			if (bottler.isFillRecipe) {
				graphics.blit(this.textureFile, this.leftPos + 108, this.topPos + 35, 177, 74, progressArrow, 16);
			} else {
				graphics.blit(this.textureFile, this.leftPos + 46, this.topPos + 35, 177, 74, progressArrow, 16);
			}
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("bottler");
		addPowerLedger(this.tile.getEnergyManager());
	}
}
