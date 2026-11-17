package forestry.core.content.sorting.gui.widgets;

import com.google.common.collect.ImmutableSet;
import forestry.api.IForestryApi;
import forestry.api.client.IForestryClientApi;
import forestry.api.core.tooltips.ToolTip;
import forestry.api.core.genetics.filter.IFilterLogic;
import forestry.api.core.genetics.filter.IFilterRuleType;
import forestry.core.platform.gui.ForestryScreen;
import forestry.core.platform.gui.widgets.Widget;
import forestry.core.platform.gui.widgets.WidgetManager;
import forestry.core.platform.util.SoundUtil;
import forestry.core.content.sorting.gui.GeneticFilterScreen;
import forestry.core.content.sorting.gui.ISelectableProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class RuleWidget extends Widget implements ISelectableProvider<IFilterRuleType> {
	private static final ImmutableSet<IFilterRuleType> ENTRIES = createEntries();

	private final Direction facing;
	private final GeneticFilterScreen gui;

	public RuleWidget(WidgetManager manager, int xPos, int yPos, Direction facing, GeneticFilterScreen gui) {
		super(manager, xPos, yPos);
		this.facing = facing;
		this.gui = gui;
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		int x = this.xPos + startX;
		int y = this.yPos + startY;
		IFilterLogic logic = this.gui.getLogic();
		IFilterRuleType rule = logic.getRule(this.facing);
		draw(this.manager.gui, rule, graphics, y, x);

		if (this.gui.selection.isSame(this)) {
			graphics.blit(SelectionWidget.TEXTURE, x - 1, y - 1, 212, 0, 18, 18);
		}
	}

	@Override
	public Collection<IFilterRuleType> getEntries() {
		return ENTRIES;
	}

	@Override
	public void draw(ForestryScreen<?> gui, IFilterRuleType selectable, GuiGraphics graphics, int y, int x) {
		TextureAtlasSprite sprite = IForestryClientApi.INSTANCE.getTextureManager().getSprite(selectable.getSprite());
		graphics.blit(x, y, 0, 16, 16, sprite);
	}

	@Override
	public Component getName(IFilterRuleType selectable) {
		return Component.translatable("for.gui.filter." + selectable.getId());
	}

	@Override
	public void onSelect(IFilterRuleType selectable) {
		IFilterLogic logic = this.gui.getLogic();
		if (logic.setRule(this.facing, selectable)) {
			logic.sendToServer(this.facing, selectable);
		}
		if (this.gui.selection.isSame(this)) {
            this.gui.onModuleClick(this);
		}
		SoundUtil.playButtonClick();
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton == 1) {
			onSelect(IForestryApi.INSTANCE.getFilterManager().getDefaultRule());
		} else {
			SoundUtil.playButtonClick();
            this.gui.onModuleClick(this);
		}
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		IFilterLogic logic = this.gui.getLogic();
		IFilterRuleType rule = logic.getRule(this.facing);
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
