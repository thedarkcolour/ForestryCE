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
package forestry.core.gui.ledgers;

import forestry.api.client.IForestryClientApi;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ClimateHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * A ledger containing climate information.
 */
public class ClimateLedger extends Ledger {
	private final IClimateProvider climateProvider;

	public ClimateLedger(LedgerManager manager, IClimateProvider climateProvider) {
		super(manager, "climate");
		this.climateProvider = climateProvider;
		maxHeight = 72;
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {
		TemperatureType temperature = climateProvider.temperature();

		// Draw background
		drawBackground(graphics, y, x);

		// Draw icon
		drawSprite(graphics, IForestryClientApi.INSTANCE.getTextureManager().getSprite(temperature), x + 3, y + 4);

		if (!isFullyOpened()) {
			return;
		}

		drawHeader(graphics, Component.translatable("for.gui.climate"), x + 22, y + 8);

		drawSubheader(graphics, Component.translatable("for.gui.temperature").append(":"), x + 22, y + 20);
		drawText(graphics, ClimateHelper.toDisplay(temperature).getString(), x + 22, y + 32);

		drawSubheader(graphics, Component.translatable("for.gui.humidity").append(":"), x + 22, y + 44);
		drawText(graphics, ClimateHelper.toDisplay(climateProvider.humidity()).getString(), x + 22, y + 56);
	}

	@Override
	public Component getTooltip() {
		return Component.literal("T: ")
			.append(ClimateHelper.toDisplay(climateProvider.temperature()))
			.append(Component.literal(" / H: "))
			.append(ClimateHelper.toDisplay(climateProvider.humidity()));
	}

}
