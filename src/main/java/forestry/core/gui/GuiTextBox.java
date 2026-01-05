package forestry.core.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class GuiTextBox extends EditBox {
	private static final int enabledColor = 14737632;
	private static final int disabledColor = 7368816;

	private final Font font;
	private final int startX, startY, width, height;

	private int lineScroll = 0;
	private int maxLines = 0;

	public GuiTextBox(Font font, int startX, int startY, int width, int height) {
		super(font, startX, startY, width, height, null);
		this.font = font;
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
	}

	private int getLineScrollOffset() {
		return 0;
	}

	public void advanceLine() {
		if (this.lineScroll < this.maxLines - 1) {
            this.lineScroll++;
		}
	}

	public void regressLine() {
		if (this.lineScroll > 0) {
            this.lineScroll--;
		}
	}

	public boolean moreLinesAllowed() {
		return this.font.split(Component.literal(getCursoredText()), this.width).size() * this.font.lineHeight < this.height;
	}

	private String getCursoredText() {
		if (!isFocused()) {
			return getValue();
		}

		int cursorPos = getCursorPosition() - getLineScrollOffset();
		String text = getValue();
		if (cursorPos < 0) {
			return text;
		}
		if (cursorPos >= text.length()) {
			return text + "_";
		}
		return text.substring(0, cursorPos) + "_" + text.substring(cursorPos);
	}

	private void drawScrolledSplitString(GuiGraphics graphics, Component text, int startX, int startY, int width, int textColour) {
		List<FormattedCharSequence> lines = this.font.split(text, width);
        this.maxLines = lines.size();

		int count = 0;
		int lineY = startY;

		for (FormattedCharSequence line : lines) {
			if (count < this.lineScroll) {
				count++;
				continue;
			} else if (lineY + this.font.lineHeight - startY > this.height) {
				break;
			}

			graphics.drawString(this.font, line, startX, lineY, textColour);
			lineY += this.font.lineHeight;

			count++;
		}

	}

	//TODO gui, rendering, I have no idea where these methods have gone
	//	@Override
	//	public void drawTextBox() {
	//		if (!getVisible()) {
	//			return;
	//		}
	//
	//		if (getEnableBackgroundDrawing()) {	//TODO AT
	//			drawRect(startX - 1, startY - 1, startX + this.width + 1, startY + this.height + 1, -6250336);
	//			drawRect(startX, startY, startX + this.width, startY + this.height, -16777216);
	//		}
	//
	//		int textColour = isFocused() ? enabledColor : disabledColor;
	//
	//		drawScrolledSplitString(getCursoredText(), startX + 2, startY + 2, width - 4, textColour);
	//	}

}
