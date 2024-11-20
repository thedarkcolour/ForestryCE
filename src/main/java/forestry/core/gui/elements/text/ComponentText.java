package forestry.core.gui.elements.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

class ComponentText extends AbstractTextElement<Component, ComponentText> {
	public ComponentText(Component component) {
		super(component);
	}

	@Override
	public LabelElement setValue(Object text) {
		if (text instanceof Component component) {
			this.text = component;
		} else if (text instanceof String string) {
			this.text = Component.literal(string);
		}
		requestLayout();
		return this;
	}

	@Override
	public void drawElement(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(FONT_RENDERER, this.text, 0, 0, 0, this.shadow);
	}

	@Override
	protected int calcWidth(Font font) {
		return font.width(text);
	}
}
