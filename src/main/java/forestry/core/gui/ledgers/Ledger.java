package forestry.core.gui.ledgers;

import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.ForestryConstants;
import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.core.config.Constants;
import forestry.core.config.SessionVars;
import forestry.core.gui.GuiForestry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

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
	protected int x;
	protected int y;

	private final ResourceLocation texture;

	protected Ledger(LedgerManager manager, String name) {
		this(manager, name, true);
	}

	protected Ledger(LedgerManager manager, String name, boolean rightSide) {
		this.manager = manager;
		if (rightSide) {
            this.texture = ledgerTextureRight;
		} else {
            this.texture = ledgerTextureLeft;
		}

        this.fontColorHeader = manager.gui.getFontColor().get("ledger." + name + ".header");
        this.fontColorSubheader = manager.gui.getFontColor().get("ledger." + name + ".subheader");
        this.fontColorText = manager.gui.getFontColor().get("ledger." + name + ".text");
        this.overlayColor = manager.gui.getFontColor().get("ledger." + name + ".background");

        this.maxWidth = Math.min(124, manager.getMaxWidth());
        this.maxTextWidth = this.maxWidth - 18;
	}

	public Rect2i getArea() {
		GuiForestry gui = this.manager.gui;
		return new Rect2i(gui.getGuiLeft() + this.x, gui.getGuiTop() + this.y, (int) this.currentWidth, (int) this.currentHeight);
	}

	// adjust the update's move amount to match the look of 60 fps (16.67 ms per update)
	private static final float msPerUpdate = 16.667f;
	private long lastUpdateTime = 0;

	public void update() {

		long updateTime;
		if (this.lastUpdateTime == 0) {
            this.lastUpdateTime = System.currentTimeMillis();
			updateTime = this.lastUpdateTime + Math.round(msPerUpdate);
		} else {
			updateTime = System.currentTimeMillis();
		}

		float moveAmount = guiTabSpeed * (updateTime - this.lastUpdateTime) / msPerUpdate;

        this.lastUpdateTime = updateTime;

		// Width
		if (this.open && this.currentWidth < this.maxWidth) {
            this.currentWidth += moveAmount;
			if (this.currentWidth > this.maxWidth) {
                this.currentWidth = this.maxWidth;
			}
		} else if (!this.open && this.currentWidth > minWidth) {
            this.currentWidth -= moveAmount;
			if (this.currentWidth < minWidth) {
                this.currentWidth = minWidth;
			}
		}

		// Height
		if (this.open && this.currentHeight < this.maxHeight) {
            this.currentHeight += moveAmount;
			if (this.currentHeight > this.maxHeight) {
                this.currentHeight = this.maxHeight;
			}
		} else if (!this.open && this.currentHeight > minHeight) {
            this.currentHeight -= moveAmount;
			if (this.currentHeight < minHeight) {
                this.currentHeight = minHeight;
			}
		}
	}

	public int getHeight() {
		return Math.round(this.currentHeight);
	}

	public int getWidth() {
		return Math.round(this.currentWidth);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final void draw(GuiGraphics graphics) {
		draw(graphics, this.y, this.x);
	}

	public abstract void draw(GuiGraphics graphics, int y, int x);

	public abstract Component getTooltip();

	public boolean handleMouseClicked(double x, double y, int mouseButton) {
		return false;
	}

	public boolean intersects(double mouseX, double mouseY) {
		return mouseX >= this.currentShiftX && mouseX <= this.currentShiftX + this.currentWidth && mouseY >= this.currentShiftY && mouseY <= this.currentShiftY + getHeight();
	}

	public void setFullyOpen() {
        this.open = true;
        this.currentWidth = this.maxWidth;
        this.currentHeight = this.maxHeight;
	}

	public void toggleOpen() {
		if (this.open) {
            this.open = false;
			SessionVars.setOpenedLedger(null);
		} else {
            this.open = true;
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
		return this.currentWidth >= this.maxWidth;
	}

	public void onGuiClosed() {

	}

	protected void drawBackground(GuiGraphics graphics, int y, int x) {
		float colorR = (this.overlayColor >> 16 & 255) / 255.0F;
		float colorG = (this.overlayColor >> 8 & 255) / 255.0F;
		float colorB = (this.overlayColor & 255) / 255.0F;

		RenderSystem.setShaderColor(colorR, colorG, colorB, 1.0F);
		RenderSystem.setShaderTexture(0, this.texture);

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
		return drawShadowText(graphics, string, x, y, this.fontColorHeader);
	}

	protected int drawSubheader(GuiGraphics graphics, Component string, int x, int y) {
		return drawShadowText(graphics, string, x, y, this.fontColorSubheader);
	}

	protected int drawShadowText(GuiGraphics graphics, Component string, int x, int y, int color) {
		return drawSplitText(graphics, string, x, y, this.maxTextWidth, color, true);
	}

	protected int drawSplitText(GuiGraphics graphics, Component string, int x, int y, int width) {
		return drawSplitText(graphics, string, x, y, width, this.fontColorText, false);
	}

	protected int drawSplitText(GuiGraphics graphics, Component string, int x, int y, int width, int color, boolean shadow) {
		int originalY = y;
		Minecraft mc = Minecraft.getInstance();
		List<FormattedCharSequence> strings = mc.font.split(string, width);
		for (FormattedCharSequence obj : strings) {
			graphics.drawString(mc.font, obj, x, y, color, shadow);
			y += mc.font.lineHeight;
		}
		return y - originalY;
	}

	protected int drawText(GuiGraphics graphics, String string, int x, int y) {
		Minecraft mc = Minecraft.getInstance();
		graphics.drawString(mc.font, string, x, y, this.fontColorText, false);
		return mc.font.lineHeight;
	}
}
