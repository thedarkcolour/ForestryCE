package forestry.apiculture.gui;

import forestry.apiculture.multiblock.IAlvearyControllerInternal;
import forestry.apiculture.multiblock.TileAlveary;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.render.EnumTankLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiAlveary extends GuiForestryTitled<ContainerAlveary> {
	private final TileAlveary tile;

	public GuiAlveary(ContainerAlveary container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/alveary.png", container, inventory, title);
		this.tile = container.getTile();
		this.imageHeight = 190;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		IAlvearyControllerInternal alvearyController = this.tile.getMultiblockLogic().getController();
		drawHealthMeter(graphics, this.leftPos + 20, this.topPos + 37, alvearyController.getHealthScaled(46), EnumTankLevel.rateTankLevel(alvearyController.getHealthScaled(100)));
	}

	private void drawHealthMeter(GuiGraphics graphics, int x, int y, int height, EnumTankLevel rated) {
		int i = 176 + rated.getLevelScaled(16);
		int k = 0;

		graphics.blit(this.textureFile, x, y + 46 - height, i, k + 46 - height, 4, height);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addClimateLedger(this.tile);
		addHintLedger("apiary");
		addOwnerLedger(this.tile);
	}
}
