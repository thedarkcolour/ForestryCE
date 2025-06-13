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

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import forestry.factory.tiles.TileStill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiStill extends GuiForestryTitled<StillMenu> {
	private final TileStill tile;

	public GuiStill(StillMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/still.png", container, inventory, title);
		this.tile = container.getTile();
        this.widgetManager.add(new TankWidget(this.widgetManager, 35, 15, 0));
        this.widgetManager.add(new TankWidget(this.widgetManager, 125, 15, 1));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		graphics.blit(this.textureFile, this.leftPos + 81, this.topPos + 57, 176, 60, 14, 14);

		if (this.tile.getStepCounter() > 0) {
			int massRemaining = this.tile.getProgressScaled(16);
			graphics.blit(this.textureFile, this.leftPos + 84, this.topPos + 17 + massRemaining, 176, 74 + massRemaining, 4, 17 - massRemaining);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("still");
		addPowerLedger(this.tile.getEnergyManager());
	}
}
