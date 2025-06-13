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
import forestry.core.gui.widgets.ReservoirWidget;
import forestry.factory.tiles.TileFabricator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiFabricator extends GuiForestryTitled<FabricatorMenu> {
	private final TileFabricator tile;

	public GuiFabricator(FabricatorMenu container, Inventory player, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/fabricator.png", container, player, title);

		this.tile = container.getTile();
		this.imageHeight = 211;
		this.widgetManager.add(new ReservoirWidget(this.widgetManager, 26, 48, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		int heatScaled = this.tile.getHeatScaled(52);
		if (heatScaled > 0) {
			graphics.blit(this.textureFile, this.leftPos + 55, this.topPos + 17 + 52 - heatScaled, 192, 52 - heatScaled, 4, heatScaled);
		}

		int meltingPointScaled = this.tile.getMeltingPointScaled(52);
		if (meltingPointScaled > 0) {
			graphics.blit(this.textureFile, this.leftPos + 52, this.topPos + 15 + 52 - meltingPointScaled, 196, 0, 10, 5);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addPowerLedger(this.tile.getEnergyManager());
		addHintLedger("fabricator");
	}
}
