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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.core.gui.GuiUtil;
import forestry.energy.ForestryEnergyStorage;

public class PowerLedger extends Ledger {
	private final ForestryEnergyStorage energyStorage;

	public PowerLedger(LedgerManager manager, ForestryEnergyStorage energyStorage) {
		super(manager, "power");
		this.energyStorage = energyStorage;
		maxHeight = 94;
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {
		// Draw background
		drawBackground(graphics, y, x);

		// Draw icon
		drawSprite(graphics, IForestryClientApi.INSTANCE.getTextureManager().getSprite(ForestrySprites.MISC_ENERGY), x + 3, y + 4);

		if (!isFullyOpened()) {
			return;
		}

		int xHeader = x + 22;
		int xBody = x + 12;

		drawHeader(graphics, Component.translatable("for.gui.energy"), xHeader, y + 8);

		drawSubheader(graphics, Component.translatable("for.gui.stored").append(":"), xBody, y + 20);
		drawText(graphics, GuiUtil.formatEnergyValue(energyStorage.getEnergyStored()), xBody, y + 32);

		drawSubheader(graphics, Component.translatable("for.gui.maxenergy").append(":"), xBody, y + 44);
		drawText(graphics, GuiUtil.formatEnergyValue(energyStorage.getMaxEnergyStored()), xBody, y + 56);

		drawSubheader(graphics, Component.translatable("for.gui.maxenergyreceive").append(":"), xBody, y + 68);
		drawText(graphics, GuiUtil.formatEnergyValue(energyStorage.getMaxEnergyReceived()), xBody, y + 80);
	}

	@Override
	public Component getTooltip() {
		return Component.literal(GuiUtil.formatEnergyValue(energyStorage.getEnergyStored()));
	}

}
