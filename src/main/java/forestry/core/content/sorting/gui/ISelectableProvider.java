package forestry.core.content.sorting.gui;

import forestry.core.platform.gui.ForestryScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public interface ISelectableProvider<S> {
	Collection<S> getEntries();

	void onSelect(S selectable);

	void draw(ForestryScreen<?> gui, S selectable, GuiGraphics graphics, int y, int x);

	Component getName(S selectable);
}
