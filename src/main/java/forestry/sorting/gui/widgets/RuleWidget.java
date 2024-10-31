package forestry.sorting.gui.widgets;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import forestry.api.IForestryApi;
import forestry.api.client.IForestryClientApi;
import forestry.api.core.tooltips.ToolTip;
import forestry.api.genetics.filter.IFilterLogic;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.utils.SoundUtil;
import forestry.sorting.gui.GuiGeneticFilter;
import forestry.sorting.gui.ISelectableProvider;

public class RuleWidget extends Widget implements ISelectableProvider<IFilterRuleType> {
	private static final ImmutableSet<IFilterRuleType> ENTRIES = createEntries();

	private final Direction facing;
	private final GuiGeneticFilter gui;

	public RuleWidget(WidgetManager manager, int xPos, int yPos, Direction facing, GuiGeneticFilter gui) {
		super(manager, xPos, yPos);
		this.facing = facing;
		this.gui = gui;
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		int x = xPos + startX;
		int y = yPos + startY;
		IFilterLogic logic = gui.getLogic();
		IFilterRuleType rule = logic.getRule(facing);
		draw(manager.gui, rule, graphics, y, x);

		if (this.gui.selection.isSame(this)) {
			graphics.blit(SelectionWidget.TEXTURE, x - 1, y - 1, 212, 0, 18, 18);
		}
	}

	@Override
	public Collection<IFilterRuleType> getEntries() {
		return ENTRIES;
	}

	@Override
	public void draw(GuiForestry<?> gui, IFilterRuleType selectable, GuiGraphics graphics, int y, int x) {
		TextureAtlasSprite sprite = IForestryClientApi.INSTANCE.getTextureManager().getSprite(selectable.getSprite());
		graphics.blit(x, y, 0, 16, 16, sprite);
	}

	@Override
	public Component getName(IFilterRuleType selectable) {
		return Component.translatable("for.gui.filter." + selectable.getId());
	}

	@Override
	public void onSelect(IFilterRuleType selectable) {
		IFilterLogic logic = gui.getLogic();
		if (logic.setRule(facing, selectable)) {
			logic.sendToServer(facing, selectable);
		}
		if (gui.selection.isSame(this)) {
			gui.onModuleClick(this);
		}
		SoundUtil.playButtonClick();
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton == 1) {
			onSelect(IForestryApi.INSTANCE.getFilterManager().getDefaultRule());
		} else {
			SoundUtil.playButtonClick();
			gui.onModuleClick(this);
		}
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		IFilterLogic logic = gui.getLogic();
		IFilterRuleType rule = logic.getRule(facing);
		ToolTip tooltip = new ToolTip();
		tooltip.add(getName(rule));
		return tooltip;
	}

	private static ImmutableSet<IFilterRuleType> createEntries() {
		ImmutableSet.Builder<IFilterRuleType> entries = ImmutableSet.builder();
		for (IFilterRuleType rule : IForestryApi.INSTANCE.getFilterManager().getRules()) {
			entries.add(rule);
		}
		return entries.build();
	}
}
