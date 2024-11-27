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
package forestry.core.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.systems.RenderSystem;

import forestry.api.ForestryConstants;
import forestry.core.config.Constants;
import forestry.core.utils.RenderUtil;

public class GuiBetterButton extends Button {
	public static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/buttons.png");

	protected final IButtonTextureSet texture;

	public GuiBetterButton(int x, int y, IButtonTextureSet texture, OnPress handler) {
		super(x, y, texture.getWidth(), texture.getHeight(), Component.empty(), handler, DEFAULT_NARRATION);
		this.texture = texture;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mX, int mY, float partialTick) {
		int xOffset = this.texture.getX();
		int yOffset = this.texture.getY();
		int h = this.height;
		int w = this.width;

		// VANILLA COPY EXCEPT FOR TEXTURE AND COORDINATES
		Minecraft minecraft = Minecraft.getInstance();
		graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blit(TEXTURE, getX(), getY(), xOffset, yOffset + RenderUtil.getYImage(this) * h, w, h);
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = getFGColor();
		this.renderString(graphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
	}
}
