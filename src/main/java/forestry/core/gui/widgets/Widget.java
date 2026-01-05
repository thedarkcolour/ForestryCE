package forestry.core.gui.widgets;

import forestry.api.core.tooltips.IToolTipProvider;
import forestry.api.core.tooltips.ToolTip;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

/**
 * Basic non-ItemStack slot
 */
public abstract class Widget implements IToolTipProvider {
	protected final WidgetManager manager;
	protected final int xPos;
	protected final int yPos;
	protected int width = 16;
	protected int height = 16;

	public Widget(WidgetManager manager, int xPos, int yPos) {
		this.manager = manager;
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int getX() {
		return this.xPos;
	}

	public int getY() {
		return this.yPos;
	}

	public abstract void draw(GuiGraphics graphics, int startX, int startY);

	public void update(int mouseX, int mouseY) {
	}

	@Nullable
	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		return null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= this.xPos && mouseX <= this.xPos + this.width && mouseY >= this.yPos && mouseY <= this.yPos + this.height;
	}

	@Override
	public boolean isHovering(double mouseX, double mouseY) {
		return isMouseOver(mouseX, mouseY);
	}

	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
	}

	public boolean handleMouseRelease(double mouseX, double mouseY, int eventType) {
		return false;
	}

	public void handleMouseMove(int mouseX, int mouseY, int mouseButton, long time) {
	}
}
