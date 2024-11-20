/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.factory.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.vertex.PoseStack;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import forestry.core.render.ColourProperties;
import forestry.factory.tiles.TileBottler;

public class GuiBottler extends GuiForestryTitled<ContainerBottler> {
	private final TileBottler tile;

	public GuiBottler(ContainerBottler container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/bottler.png", container, inventory, title);
		this.tile = container.getTile();
		widgetManager.add(new TankWidget(this.widgetManager, 80, 14, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		bindTexture(textureFile);

		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		graphics.blit(this.textureFile, x, y, 0, 0, imageWidth, imageHeight);

		//RenderHelper.enableGUIStandardItemLighting(); TODO Gui Light
		// RenderSystem.disableLighting();
		// RenderSystem.enableRescaleNormal();
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		PoseStack pose = graphics.pose();
		pose.pushPose();
		{
			pose.translate(leftPos, topPos, 0.0F);
			drawWidgets(graphics);
		}
		pose.popPose();

		Component name = tile.getTitle();
		textLayout.line = 5;
		textLayout.drawCenteredLine(graphics, name, 0, ColourProperties.INSTANCE.get("gui.title"));
		bindTexture(textureFile);

		bindTexture(textureFile);

		TileBottler bottler = tile;
		int progressArrow = bottler.getProgressScaled(22);
		if (progressArrow > 0) {
			if (bottler.isFillRecipe) {
				graphics.blit(this.textureFile, leftPos + 108, topPos + 35, 177, 74, progressArrow, 16);
			} else {
				graphics.blit(this.textureFile, leftPos + 46, topPos + 35, 177, 74, progressArrow, 16);
			}
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(tile);
		addHintLedger("bottler");
		addPowerLedger(tile.getEnergyManager());
	}
}
