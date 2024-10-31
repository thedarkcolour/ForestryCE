package forestry.sorting.gui.widgets;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;

import forestry.api.IForestryApi;
import forestry.api.core.tooltips.ToolTip;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.genetics.filter.IFilterLogic;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.GuiUtil;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SoundUtil;
import forestry.sorting.gui.GuiGeneticFilter;
import forestry.sorting.gui.ISelectableProvider;

public class SpeciesWidget extends Widget implements ISelectableProvider<ISpecies<?>> {
	private final static IdentityHashMap<ISpecies<?>, ItemStack> ITEMS = createEntries();
	private final ImmutableSet<ISpecies<?>> entries;

	private final Direction facing;
	private final int index;
	private final boolean active;
	private final GuiGeneticFilter gui;

	public SpeciesWidget(WidgetManager manager, int xPos, int yPos, Direction facing, int index, boolean active, GuiGeneticFilter gui) {
		super(manager, xPos, yPos);
		this.facing = facing;
		this.index = index;
		this.active = active;
		this.gui = gui;
		ImmutableSet.Builder<ISpecies<?>> entries = ImmutableSet.builder();

		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			IBreedingTracker tracker = type.getBreedingTracker(manager.minecraft.level, manager.minecraft.player.getGameProfile());

			for (ResourceLocation id : tracker.getDiscoveredSpecies()) {
				ISpecies<?> species = type.getSpeciesSafe(id);

				if (species != null) {
					entries.add(species);
				}
			}
		}
		this.entries = entries.build();
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		int x = xPos + startX;
		int y = yPos + startY;
		IFilterLogic logic = gui.getLogic();
		ISpecies<?> allele = logic.getGenomeFilter(facing, index, active);
		if (allele != null) {
			GuiUtil.drawItemStack(graphics, manager.gui, ITEMS.getOrDefault(allele, ItemStack.EMPTY), x, y);
		}

		if (this.gui.selection.isSame(this)) {
			RenderSystem.setShaderTexture(0, SelectionWidget.TEXTURE);
			graphics.blit(SelectionWidget.TEXTURE, x - 1, y - 1, 212, 0, 18, 18);
		}
	}

	@Override
	public ImmutableSet<ISpecies<?>> getEntries() {
		return entries;
	}

	@Override
	public void onSelect(@Nullable ISpecies<?> selectable) {
		IFilterLogic logic = gui.getLogic();
		if (logic.setGenomeFilter(facing, index, active, selectable)) {
			logic.sendToServer(facing, (short) index, active, selectable);
		}
		if (gui.selection.isSame(this)) {
			gui.onModuleClick(this);
		}
		SoundUtil.playButtonClick();
	}

	@Override
	public void draw(GuiForestry<?> gui, ISpecies<?> selectable, GuiGraphics graphics, int y, int x) {
		GuiUtil.drawItemStack(graphics, gui, ITEMS.getOrDefault(selectable, ItemStack.EMPTY), x, y);
	}

	@Override
	public Component getName(ISpecies<?> selectable) {
		return selectable.getDisplayName();
	}

	@Nullable
	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		IFilterLogic logic = gui.getLogic();
		ISpecies<?> allele = logic.getGenomeFilter(facing, index, active);
		if (allele == null) {
			return null;
		}
		ToolTip tooltip = new ToolTip();
		tooltip.add(getName(allele));
		return tooltip;
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		ItemStack stack = gui.getMinecraft().player.inventoryMenu.getCarried();
		if (!stack.isEmpty()) {
			IIndividual individual = IIndividualHandlerItem.getIndividual(stack);

			if (individual != null) {
				onSelect(mouseButton == 0 ? individual.getSpecies() : individual.getInactiveSpecies());
				return;
			}
		}
		if (mouseButton == 1) {
			onSelect(null);
		} else {
			SoundUtil.playButtonClick();
			gui.onModuleClick(this);
		}
	}

	private static IdentityHashMap<ISpecies<?>, ItemStack> createEntries() {
		IdentityHashMap<ISpecies<?>, ItemStack> entries = new IdentityHashMap<>();

		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			GeneticsUtil.getIconStacks(entries, type.getDefaultStage(), type);
		}
		return entries;
	}
}
