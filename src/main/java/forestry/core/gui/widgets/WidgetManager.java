package forestry.core.gui.widgets;

import forestry.core.gui.GuiForestry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WidgetManager {
	public final GuiForestry gui;
	public final Minecraft minecraft;
	protected final List<Widget> widgets = new ArrayList<>();

	public WidgetManager(GuiForestry gui) {
		this.gui = gui;
		this.minecraft = Minecraft.getInstance();
	}

	public void add(Widget slot) {
		this.widgets.add(slot);
	}

	public void remove(Widget slot) {
		this.widgets.remove(slot);
	}

	public void clear() {
		this.widgets.clear();
	}

	public List<Widget> getWidgets() {
		return this.widgets;
	}

	@Nullable
	public Widget getAtPosition(double mX, double mY) {
		for (Widget slot : this.widgets) {
			if (slot.isMouseOver(mX, mY)) {
				return slot;
			}
		}

		return null;
	}

	public void drawWidgets(GuiGraphics graphics) {
		for (Widget slot : this.widgets) {
			slot.draw(graphics, 0, 0);
		}
	}

	public void updateWidgets(int mouseX, int mouseY) {
		for (Widget slot : this.widgets) {
			slot.update(mouseX, mouseY);
		}
	}

	public void handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		Widget slot = getAtPosition(mouseX - this.gui.getGuiLeft(), mouseY - this.gui.getGuiTop());
		if (slot != null) {
			slot.handleMouseClick(mouseX, mouseY, mouseButton);
		}
	}

	public boolean handleMouseRelease(double mouseX, double mouseY, int eventType) {
		boolean hasToStop = false;
		for (Widget slot : this.widgets) {
			hasToStop |= slot.handleMouseRelease(mouseX - this.gui.getGuiLeft(), mouseY - this.gui.getGuiTop(), eventType);
		}
		return hasToStop;
	}
}
