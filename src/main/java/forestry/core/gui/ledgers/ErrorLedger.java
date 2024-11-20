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

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import forestry.api.core.IError;
import forestry.core.utils.StringUtil;

/**
 * A ledger displaying error messages and help text.
 */
public class ErrorLedger extends Ledger {

	@Nullable
	private IError state;

	public ErrorLedger(LedgerManager manager) {
		super(manager, "error", false);
		maxHeight = 72;
	}

	public void setState(@Nullable IError state) {
		this.state = state;
		if (state != null) {
			int lineHeight = StringUtil.getLineHeight(maxTextWidth, getTooltip(), Component.translatable(state.getHelpTranslationKey()));
			maxHeight = lineHeight + 20;
		}
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {
		if (state == null) {
			return;
		}

		// Draw background
		drawBackground(graphics, y, x);
		y += 4;

		int xIcon = x + 5;
		int xBody = x + 14;
		int xHeader = x + 24;

		// Draw sprite
		drawSprite(graphics, state.getSprite(), xIcon, y);
		y += 4;

		// Write description if fully opened
		if (isFullyOpened()) {
			y += drawHeader(graphics, getTooltip(), xHeader, y);
			y += 4;

			Component helpString = Component.translatable(state.getHelpTranslationKey());
			drawSplitText(graphics, helpString, xBody, y, maxTextWidth);
		}
	}

	@Override
	public boolean isVisible() {
		return state != null;
	}

	@Override
	public Component getTooltip() {
		if (state == null) {
			return Component.literal("");
		}
		return Component.translatable(state.getDescriptionTranslationKey());
	}

}
