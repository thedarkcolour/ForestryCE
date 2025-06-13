package forestry.sorting.gui;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;

public class GuiGeneticFilter extends GuiForestryTitled<GeneticFilterMenu> {
	private final TileGeneticFilter tile;
	private final WidgetScrollBar scrollBar;
	public final SelectionWidget selection;
	@Nullable
	private EditBox searchField;

	public GuiGeneticFilter(GeneticFilterMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/filter.png", container, inventory, title);
        this.imageHeight = 222;
        this.imageWidth = 212;
		this.tile = container.getTile();

		for (int i = 0; i < 6; i++) {
			Direction facing = Direction.from3DDataValue(i);
            this.widgetManager.add(new RuleWidget(this.widgetManager, 8 + 36, 18 + i * 18, facing, this));
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 2; k++) {
                    this.widgetManager.add(new SpeciesWidget(this.widgetManager, 44 + 36 + j * 45 + k * 18, 18 + i * 18, Direction.from3DDataValue(i), j, k == 0, this));
				}
			}
		}

		this.scrollBar = new WidgetScrollBar(this.widgetManager, 157 + 36, 150, 12, 64, new Drawable(new ResourceLocation(Constants.TEXTURE_PATH_GUI + "/container/creative_inventory/tabs.png"), 232, 0, 12, 15));
        this.widgetManager.add(this.selection = new SelectionWidget(this.widgetManager, 0, 134, this.scrollBar, this));
        this.widgetManager.add(this.scrollBar);
        this.scrollBar.setVisible(false);
	}

	public <S> void onModuleClick(ISelectableProvider<S> provider) {
		if (this.selection.isSame(provider)) {
			deselectFilter();
		} else {
			selectFilter(provider);
		}
	}

	private <S> void selectFilter(ISelectableProvider<S> provider) {
        this.selection.setProvider(provider);
		if (this.searchField != null) {
            this.searchField.setEditable(true);
            this.searchField.setVisible(true);
		}
        this.selection.filterEntries(this.searchField != null ? this.searchField.getValue() : "");
		for (Slot slot : this.menu.slots) {
			if (slot instanceof SlotGeneticFilter filter) {
				filter.setEnabled(false);
			}
		}
	}

	private void deselectFilter() {
		this.selection.setProvider(null);
		if (this.searchField != null) {
            this.searchField.setEditable(false);
            this.searchField.setVisible(false);
		}
        this.scrollBar.setVisible(false);
		for (Slot slot : this.menu.slots) {
			if (slot instanceof SlotGeneticFilter filter) {
				filter.setEnabled(true);
			}
		}
	}

	@Override
	public void init() {
		super.init();

		String oldString = this.searchField != null ? this.searchField.getValue() : "";

		this.searchField = new EditBox(this.font, this.leftPos + this.selection.getX() + 89 + 36, this.selection.getY() + this.topPos + 4, 80, this.font.lineHeight, null);
		this.searchField.setMaxLength(50);
		this.searchField.setBordered(false);
		this.searchField.setTextColor(16777215);
		this.searchField.setValue(oldString);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.searchField != null) {
			this.searchField.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (this.searchField != null && this.searchField.keyPressed(key, scanCode, modifiers)) {
            this.scrollBar.setValue(0);
            this.selection.filterEntries(this.searchField.getValue());
			return true;
		} else {
			return super.keyPressed(key, scanCode, modifiers);
		}
	}

	@Nullable
	@Override
	protected Slot getSlotAtPosition(double mouseX, double mouseY) {
		Slot slot = super.getSlotAtPosition(mouseX, mouseY);
		if (slot instanceof SlotGeneticFilter && this.selection.getLogic() != null) {
			return null;
		}
		return slot;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}

		if (this.searchField != null) {
            this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		Widget widget = this.widgetManager.getAtPosition(mouseX - this.leftPos, mouseY - this.topPos);
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
		return this.tile.getLogic();
	}
}
