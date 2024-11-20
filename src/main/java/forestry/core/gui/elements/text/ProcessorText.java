package forestry.core.gui.elements.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

class ProcessorText extends AbstractTextElement<FormattedCharSequence, ProcessorText> {
	public ProcessorText(FormattedCharSequence component) {
		super(component);
	}

	public ProcessorText(int xPos, int yPos, int width, int height, FormattedCharSequence component, boolean fitText) {
		super(xPos, yPos, width, height, component, fitText);
	}

	@Override
	public LabelElement setValue(Object text) {
		if (text instanceof FormattedCharSequence formatted) {
			this.text = formatted;
		}
		requestLayout();
		return this;
	}

	@Override
	protected int calcWidth(Font font) {
		return font.width(text);
	}

	@Override
	public void drawElement(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(FONT_RENDERER, this.text, 0, 0, 0, this.shadow);
	}
}
