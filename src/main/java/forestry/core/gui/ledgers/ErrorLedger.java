package forestry.core.gui.ledgers;

import forestry.api.core.IError;
import forestry.core.gui.GuiForestry;
import forestry.core.utils.StringUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * A ledger displaying error messages and help text.
 */
public class ErrorLedger extends Ledger {

	@Nullable
	private IError state;

	public ErrorLedger(LedgerManager manager) {
		super(manager, "error", false);
        this.maxHeight = 72;
	}

	public void setState(@Nullable IError state) {
		this.state = state;
		if (state != null) {
			int lineHeight = StringUtil.getLineHeight(this.maxTextWidth, getTooltip(), Component.translatable(state.getHelpTranslationKey()));
            this.maxHeight = lineHeight + 20;
		}
	}

	@Override
	public Rect2i getArea() {
		GuiForestry gui = this.manager.gui;
		return new Rect2i(gui.getGuiLeft() - (int) this.currentWidth, gui.getGuiTop() + this.y, (int) this.currentWidth, (int) this.currentHeight);
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {
		if (this.state == null) {
			return;
		}

		// Draw background
		drawBackground(graphics, y, x);
		y += 4;

		int xIcon = x + 5;
		int xBody = x + 14;
		int xHeader = x + 24;

		// Draw sprite
		drawSprite(graphics, this.state.getSprite(), xIcon, y);
		y += 4;

		// Write description if fully opened
		if (isFullyOpened()) {
			y += drawHeader(graphics, getTooltip(), xHeader, y);
			y += 4;

			Component helpString = Component.translatable(this.state.getHelpTranslationKey());
			drawSplitText(graphics, helpString, xBody, y, this.maxTextWidth);
		}
	}

	@Override
	public boolean isVisible() {
		return this.state != null;
	}

	@Override
	public Component getTooltip() {
		if (this.state == null) {
			return Component.literal("");
		}
		return Component.translatable(this.state.getDescriptionTranslationKey());
	}

}
