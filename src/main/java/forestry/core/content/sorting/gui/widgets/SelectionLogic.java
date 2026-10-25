package forestry.core.content.sorting.gui.widgets;

import forestry.api.core.tooltips.ToolTip;
import forestry.core.platform.gui.ForestryScreen;
import forestry.core.platform.gui.widgets.IScrollable;
import forestry.core.content.sorting.gui.ISelectableProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public class SelectionLogic<S> implements IScrollable {
	private static final int SELECTABLE_PER_ROW = 11;

	private final ISelectableProvider<S> provider;
	private final Comparator<S> comparator;
	private final SelectionWidget widget;
	private final Collection<S> entries;
	private final ArrayList<S> sorted = new ArrayList<>();
	private final Set<SelectableWidget> visible = new HashSet<>();

	public SelectionLogic(SelectionWidget widget, ISelectableProvider<S> provider) {
		this.widget = widget;
		this.provider = provider;
		this.entries = provider.getEntries();
		this.comparator = (S f, S s) -> provider.getName(f).getString().compareToIgnoreCase(provider.getName(s).getString());

	}

	public boolean isSame(ISelectableProvider provider) {
		return this.provider == provider;
	}

	@Override
	public void onScroll(int value) {
        this.visible.clear();
		int startIndex = value * SELECTABLE_PER_ROW;
		Y:
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < SELECTABLE_PER_ROW; x++) {
				int index = startIndex + y * SELECTABLE_PER_ROW + x;
				if (index >= this.sorted.size()) {
					break Y;
				}
                this.visible.add(new SelectableWidget(this.sorted.get(index), this.widget.getX() + 12 + x * 16, this.widget.getY() + 16 + y * 16));
			}
		}
	}

	public Set<SelectableWidget> getVisible() {
		return this.visible;
	}

	public void filterEntries(String searchText) {
        this.sorted.clear();
        this.sorted.ensureCapacity(this.entries.size());

		Pattern pattern;
		try {
			pattern = Pattern.compile(searchText.toLowerCase(Locale.ENGLISH), Pattern.CASE_INSENSITIVE);
		} catch (Throwable ignore) {
			try {
				pattern = Pattern.compile(Pattern.quote(searchText.toLowerCase(Locale.ENGLISH)), Pattern.CASE_INSENSITIVE);
			} catch (Throwable e) {
				return;
			}
		}

		for (S entry : this.entries) {
			Component name = this.provider.getName(entry);
			if (pattern.matcher(name.getString().toLowerCase(Locale.ENGLISH)).find()) {
                this.sorted.add(entry);
			}
		}
        this.sorted.sort(this.comparator);

		int elements = this.sorted.size() / SELECTABLE_PER_ROW - 4;
		if (elements > 0) {
            this.widget.scrollBar.setParameters(this, 0, elements, 1);
		} else {
			onScroll(0);
		}
        this.widget.scrollBar.setVisible(elements > 0);

	}

	@Override
	public boolean isFocused(int mouseX, int mouseY) {
		return this.widget.isMouseOver(mouseX, mouseY);
	}

	public void draw(GuiGraphics graphics) {
		for (SelectableWidget selectable : this.visible) {
			selectable.draw(this.widget.gui, graphics);
		}
	}

	@Nullable
	public ToolTip getToolTip(int mouseX, int mouseY) {
		for (SelectableWidget selectable : this.visible) {
			if (selectable.isMouseOver(mouseX, mouseY)) {
				return selectable.getToolTip();
			}
		}
		return null;
	}

	public void select(double mouseX, double mouseY) {
		mouseX -= this.widget.gui.getGuiLeft();
		mouseY -= this.widget.gui.getGuiTop();
		for (SelectableWidget selectable : this.visible) {
			if (selectable.isMouseOver(mouseX, mouseY)) {
                this.provider.onSelect(selectable.selectable);
				break;
			}
		}
	}

	private class SelectableWidget {
		private final S selectable;
		private final int xPos;
		private final int yPos;

		public SelectableWidget(S entry, int xPos, int yPos) {
			this.selectable = entry;
			this.xPos = xPos;
			this.yPos = yPos;
		}

		public void draw(ForestryScreen<?> gui, GuiGraphics graphics) {
            SelectionLogic.this.provider.draw(gui, this.selectable, graphics, this.yPos, this.xPos);
		}

		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= this.xPos && mouseX <= this.xPos + 16 && mouseY >= this.yPos && mouseY <= this.yPos + 16;
		}

		public ToolTip getToolTip() {
			ToolTip toolTip = new ToolTip();
			toolTip.add(SelectionLogic.this.provider.getName(this.selectable));
			return toolTip;
		}
	}
}
