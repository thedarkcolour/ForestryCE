package forestry.sorting.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import com.mojang.blaze3d.systems.RenderSystem;

import forestry.api.genetics.filter.IFilterLogic;
import forestry.core.config.Constants;
import forestry.core.gui.Drawable;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetScrollBar;
import forestry.sorting.gui.widgets.RuleWidget;
import forestry.sorting.gui.widgets.SelectionWidget;
import forestry.sorting.gui.widgets.SpeciesWidget;
import forestry.sorting.tiles.TileGeneticFilter;

public class GuiGeneticFilter extends GuiForestryTitled<ContainerGeneticFilter> {
	private final TileGeneticFilter tile;
	private final WidgetScrollBar scrollBar;
	public final SelectionWidget selection;
	@Nullable
	private EditBox searchField;

	public GuiGeneticFilter(ContainerGeneticFilter container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/filter.png", container, inventory, title);
		imageHeight = 222;
		imageWidth = 212;
		this.tile = container.getTile();

		for (int i = 0; i < 6; i++) {
			Direction facing = Direction.from3DDataValue(i);
			widgetManager.add(new RuleWidget(widgetManager, 8 + 36, 18 + i * 18, facing, this));
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 2; k++) {
					widgetManager.add(new SpeciesWidget(widgetManager, 44 + 36 + j * 45 + k * 18, 18 + i * 18, Direction.from3DDataValue(i), j, k == 0, this));
				}
			}
		}

		this.scrollBar = new WidgetScrollBar(widgetManager, 157 + 36, 150, 12, 64, new Drawable(new ResourceLocation(Constants.TEXTURE_PATH_GUI + "/container/creative_inventory/tabs.png"), 232, 0, 12, 15));
		widgetManager.add(this.selection = new SelectionWidget(widgetManager, 0, 134, scrollBar, this));
		widgetManager.add(scrollBar);
		scrollBar.setVisible(false);
	}

	public <S> void onModuleClick(ISelectableProvider<S> provider) {
		if (selection.isSame(provider)) {
			deselectFilter();
		} else {
			selectFilter(provider);
		}
	}

	private <S> void selectFilter(ISelectableProvider<S> provider) {
		selection.setProvider(provider);
		if (searchField != null) {
			searchField.setEditable(true);
			searchField.setVisible(true);
		}
		selection.filterEntries(searchField != null ? searchField.getValue() : "");
		for (Slot slot : this.menu.slots) {
			if (slot instanceof SlotGeneticFilter filter) {
				filter.setEnabled(false);
			}
		}
	}

	private void deselectFilter() {
		this.selection.setProvider(null);
		if (searchField != null) {
			searchField.setEditable(false);
			searchField.setVisible(false);
		}
		scrollBar.setVisible(false);
		for (Slot slot : this.menu.slots) {
			if (slot instanceof SlotGeneticFilter filter) {
				filter.setEnabled(true);
			}
		}
	}

	@Override
	public void init() {
		super.init();

		String oldString = searchField != null ? searchField.getValue() : "";

		this.searchField = new EditBox(this.font, this.leftPos + selection.getX() + 89 + 36, selection.getY() + this.topPos + 4, 80, this.font.lineHeight, null);
		this.searchField.setMaxLength(50);
		this.searchField.setBordered(false);
		this.searchField.setTextColor(16777215);
		this.searchField.setValue(oldString);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (searchField != null) {
			this.searchField.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (searchField != null && this.searchField.keyPressed(key, scanCode, modifiers)) {
			scrollBar.setValue(0);
			selection.filterEntries(searchField.getValue());
			return true;
		} else {
			return super.keyPressed(key, scanCode, modifiers);
		}
	}

	@Nullable
	@Override
	protected Slot getSlotAtPosition(double mouseX, double mouseY) {
		Slot slot = super.getSlotAtPosition(mouseX, mouseY);
		if (slot instanceof SlotGeneticFilter && selection.getLogic() != null) {
			return null;
		}
		return slot;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}

		if (searchField != null) {
			searchField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		Widget widget = widgetManager.getAtPosition(mouseX - leftPos, mouseY - topPos);
		if (widget == null) {
			deselectFilter();
		}
		return true;
	}

	@Override
	protected void addLedgers() {
		addHintLedger("filter");
	}

	public IFilterLogic getLogic() {
		return tile.getLogic();
	}
}
