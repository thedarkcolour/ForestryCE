package forestry.core.gui.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import forestry.core.gui.Drawable;
import forestry.core.utils.RenderUtil;

public class GuiToggleButton extends Button {
	/* attributes - Final */
	private final Drawable[] textures = new Drawable[3];

	public GuiToggleButton(int x, int y, int widthIn, int heightIn, Drawable drawable, OnPress handler) {
		super(x, y, widthIn, heightIn, Component.empty(), handler, DEFAULT_NARRATION);
		for (int i = 0; i < 3; i++) {
			textures[i] = new Drawable(drawable.textureLocation, drawable.u, drawable.v + drawable.vHeight * i, drawable.uWidth, drawable.vHeight);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mX, int mY, float partialTick) {
		int x = getX(), y = getY();

		this.isHovered = mX >= x && mY >= y && mX < x + this.width && mY < y + this.height;
		int hoverState = RenderUtil.getYImage(this);
		Drawable drawable = textures[hoverState];
		drawable.draw(graphics, y, x);
	}
}
