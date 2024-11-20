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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

import com.mojang.authlib.GameProfile;

import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.core.owner.IOwnedTile;
import forestry.core.utils.PlayerUtil;

/**
 * Ledger displaying ownership information
 */
public class OwnerLedger extends Ledger {
	@Nullable
	private final GameProfile owner;

	public OwnerLedger(LedgerManager manager, IOwnedTile tile) {
		super(manager, "owner");

		this.owner = tile.getOwnerHandler().getOwner();
		this.maxHeight = 40;
	}

	@Override
	public boolean isVisible() {
		return this.owner != null;
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {
		// Draw background
		drawBackground(graphics, y, x);

		// Draw icon
		TextureAtlasSprite accessIcon = IForestryClientApi.INSTANCE.getTextureManager().getSprite(ForestrySprites.MISC_ACCESS_SHARED);
		drawSprite(graphics, accessIcon, x + 3, y + 4);

		// Draw description
		if (isFullyOpened()) {
			drawHeader(graphics, Component.translatable("for.gui.owner"), x + 22, y + 8);
			drawText(graphics, PlayerUtil.getOwnerName(this.owner), x + 22, y + 20);
		}
	}

	@Override
	public Component getTooltip() {
		return Component.translatable("for.gui.owner")
				.append(": " + PlayerUtil.getOwnerName(this.owner));
	}
}
