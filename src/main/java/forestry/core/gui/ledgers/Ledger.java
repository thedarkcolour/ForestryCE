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

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.blaze3d.systems.RenderSystem;

import forestry.api.ForestryConstants;
import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.core.config.Constants;
import forestry.core.config.SessionVars;
import forestry.core.gui.GuiForestry;

/**
 * Side ledger for guis
 */
public abstract class Ledger {
	// Gui tabs (Ledger)
	public static final int guiTabSpeed = 8;
	protected static final int minWidth = 24;
	public static final int minHeight = 24;
	protected final int maxWidth;
	protected final int maxTextWidth;
	protected int maxHeight = 24;

	private static final ResourceLocation ledgerTextureRight = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/ledger.png");
	private static final ResourceLocation ledgerTextureLeft = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/ledger_left.png");

	protected final LedgerManager manager;

	private final int fontColorHeader;
	private final int fontColorText;
	private final int fontColorSubheader;
	private final int overlayColor;

	private boolean open;

	public int currentShiftX = 0;
	public int currentShiftY = 0;

	protected float currentWidth = minWidth;
	protected float currentHeight = minHeight;
	private int x;
	private int y;

	private final ResourceLocation texture;

	protected Ledger(LedgerManager manager, String name) {
		this(manager, name, true);
	}

	protected Ledger(LedgerManager manager, String name, boolean rightSide) {
		this.manager = manager;
		if (rightSide) {
			texture = ledgerTextureRight;
		} else {
			texture = ledgerTextureLeft;
		}

		fontColorHeader = manager.gui.getFontColor().get("ledger." + name + ".header");
		fontColorSubheader = manager.gui.getFontColor().get("ledger." + name + ".subheader");
		fontColorText = manager.gui.getFontColor().get("ledger." + name + ".text");
		overlayColor = manager.gui.getFontColor().get("ledger." + name + ".background");

		maxWidth = Math.min(124, manager.getMaxWidth());
		maxTextWidth = maxWidth - 18;
	}

	public Rect2i getArea() {
		GuiForestry gui = manager.gui;
		return new Rect2i(gui.getGuiLeft() + x, gui.getGuiTop() + y, (int) currentWidth, (int) currentHeight);
	}

	// adjust the update's move amount to match the look of 60 fps (16.67 ms per update)
	private static final float msPerUpdate = 16.667f;
	private long lastUpdateTime = 0;

	public void update() {

		long updateTime;
		if (lastUpdateTime == 0) {
			lastUpdateTime = System.currentTimeMillis();
			updateTime = lastUpdateTime + Math.round(msPerUpdate);
		} else {
			updateTime = System.currentTimeMillis();
		}

		float moveAmount = guiTabSpeed * (updateTime - lastUpdateTime) / msPerUpdate;

		lastUpdateTime = updateTime;

		// Width
		if (open && currentWidth < maxWidth) {
			currentWidth += moveAmount;
			if (currentWidth > maxWidth) {
				currentWidth = maxWidth;
			}
		} else if (!open && currentWidth > minWidth) {
			currentWidth -= moveAmount;
			if (currentWidth < minWidth) {
				currentWidth = minWidth;
			}
		}

		// Height
		if (open && currentHeight < maxHeight) {
			currentHeight += moveAmount;
			if (currentHeight > maxHeight) {
				currentHeight = maxHeight;
			}
		} else if (!open && currentHeight > minHeight) {
			currentHeight -= moveAmount;
			if (currentHeight < minHeight) {
				currentHeight = minHeight;
			}
		}
	}

	public int getHeight() {
		return Math.round(currentHeight);
	}

	public int getWidth() {
		return Math.round(currentWidth);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final void draw(GuiGraphics graphics) {
		draw(graphics, y, x);
	}

	public abstract void draw(GuiGraphics graphics, int y, int x);

	public abstract Component getTooltip();

	public boolean handleMouseClicked(double x, double y, int mouseButton) {
		return false;
	}

	public boolean intersects(double mouseX, double mouseY) {
		return mouseX >= currentShiftX && mouseX <= currentShiftX + currentWidth && mouseY >= currentShiftY && mouseY <= currentShiftY + getHeight();
	}

	public void setFullyOpen() {
		open = true;
		currentWidth = maxWidth;
		currentHeight = maxHeight;
	}

	public void toggleOpen() {
		if (open) {
			open = false;
			SessionVars.setOpenedLedger(null);
		} else {
			open = true;
			SessionVars.setOpenedLedger(this.getClass());
		}
	}

	public boolean isVisible() {
		return true;
	}

	public boolean isOpen() {
		return this.open;
	}

	protected boolean isFullyOpened() {
		return currentWidth >= maxWidth;
	}

	public void onGuiClosed() {

	}

	protected void drawBackground(GuiGraphics graphics, int y, int x) {
		float colorR = (overlayColor >> 16 & 255) / 255.0F;
		float colorG = (overlayColor >> 8 & 255) / 255.0F;
		float colorB = (overlayColor & 255) / 255.0F;

		RenderSystem.setShaderColor(colorR, colorG, colorB, 1.0F);
		RenderSystem.setShaderTexture(0, texture);

		int height = getHeight();
		int width = getWidth();

		// left edge
		graphics.blit(this.texture, x, y + 4, 0, 256 - height + 4, 4, height - 4);
		// top edge
		graphics.blit(this.texture, x + 4, y, 256 - width + 4, 0, width - 4, 4);
		// top left corner
		graphics.blit(this.texture, x, y, 0, 0, 4, 4);
		// body + bottom + right
		graphics.blit(this.texture, x + 4, y + 4, 256 - width + 4, 256 - height + 4, width - 4, height - 4);

		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0F);
	}

	protected void drawSprite(GuiGraphics graphics, ResourceLocation path, int x, int y) {
		drawSprite(graphics, IForestryClientApi.INSTANCE.getTextureManager().getSprite(path), x, y);
	}

	protected void drawSprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y) {
		drawSprite(graphics, sprite, x, y, ForestrySprites.TEXTURE_ATLAS);
	}

	protected void drawSprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, ResourceLocation textureMap) {
		graphics.blit(x, y, 0, 16, 16, sprite);
	}

	protected int drawHeader(GuiGraphics graphics, Component string, int x, int y) {
		return drawShadowText(graphics, string, x, y, fontColorHeader);
	}

	protected int drawSubheader(GuiGraphics graphics, Component string, int x, int y) {
		return drawShadowText(graphics, string, x, y, fontColorSubheader);
	}

	protected int drawShadowText(GuiGraphics graphics, Component string, int x, int y, int color) {
		return drawSplitText(graphics, string, x, y, maxTextWidth, color, true);
	}

	protected int drawSplitText(GuiGraphics graphics, Component string, int x, int y, int width) {
		return drawSplitText(graphics, string, x, y, width, fontColorText, false);
	}

	protected int drawSplitText(GuiGraphics graphics, Component string, int x, int y, int width, int color, boolean shadow) {
		int originalY = y;
		Minecraft mc = Minecraft.getInstance();
		List<FormattedCharSequence> strings = mc.font.split(string, width);
		for (FormattedCharSequence obj : strings) {
			if (shadow) {
				graphics.drawString(mc.font, obj, x, y, color);
			} else {
				graphics.drawString(mc.font, obj, x, y, color);
			}
			y += mc.font.lineHeight;
		}
		return y - originalY;
	}

	protected int drawText(GuiGraphics graphics, String string, int x, int y) {
		Minecraft mc = Minecraft.getInstance();
		graphics.drawString(mc.font, string, x, y, fontColorText);
		return mc.font.lineHeight;
	}
}
