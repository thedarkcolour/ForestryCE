package forestry.sorting.gui;

import java.util.Collection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import forestry.core.gui.GuiForestry;

public interface ISelectableProvider<S> {
	Collection<S> getEntries();

	void onSelect(S selectable);

	void draw(GuiForestry<?> gui, S selectable, GuiGraphics graphics, int y, int x);

	Component getName(S selectable);
}
