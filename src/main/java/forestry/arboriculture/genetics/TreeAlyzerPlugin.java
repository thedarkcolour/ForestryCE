/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.genetics;

import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.core.IProduct;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.gui.GuiAlyzer;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.TextLayoutHelper;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;

public enum TreeAlyzerPlugin implements IAlyzerPlugin {
	INSTANCE;

	// todo make this reloadable so that data-driven species will work properly
	private final Map<ISpecies<?>, ItemStack> iconStacks;

	TreeAlyzerPlugin() {
		this.iconStacks = GeneticsUtil.getIconStacks(TreeLifeStage.SAPLING, SpeciesUtil.TREE_TYPE.get());
	}

	@Override
	public void drawAnalyticsPage1(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, (individual, type) -> {
				if (individual instanceof ITree tree) {
					IGenome genome = tree.getGenome();

					TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

					textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

					textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
					textLayout.drawLine(graphics, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

					textLayout.newLine();
					textLayout.newLine();

					guiAlyzer.drawSpeciesRow(graphics, tree, TreeChromosomes.SPECIES);
					textLayout.newLine();

					guiAlyzer.drawChromosomeRow(graphics, tree, TreeChromosomes.SAPLINGS);
					textLayout.newLineCompressed();
					guiAlyzer.drawChromosomeRow(graphics, tree, TreeChromosomes.MATURATION);
					textLayout.newLineCompressed();
					guiAlyzer.drawChromosomeRow(graphics, tree, TreeChromosomes.HEIGHT);
					textLayout.newLineCompressed();

					int activeGirth = genome.getActiveValue(TreeChromosomes.GIRTH);
					int inactiveGirth = genome.getInactiveValue(TreeChromosomes.GIRTH);
					textLayout.drawLine(graphics, TreeChromosomes.GIRTH.getChromosomeDisplayName(), GuiAlyzer.COLUMN_0);
					guiAlyzer.drawLine(graphics, String.format("%sx%s", activeGirth, activeGirth), GuiAlyzer.COLUMN_1, tree, TreeChromosomes.GIRTH, false);
					guiAlyzer.drawLine(graphics, String.format("%sx%s", inactiveGirth, inactiveGirth), GuiAlyzer.COLUMN_2, tree, TreeChromosomes.GIRTH, true);

					textLayout.newLineCompressed();

					guiAlyzer.drawChromosomeRow(graphics, tree, TreeChromosomes.YIELD);
					textLayout.newLineCompressed();
					guiAlyzer.drawChromosomeRow(graphics, tree, TreeChromosomes.SAPPINESS);
					textLayout.newLineCompressed();

					guiAlyzer.drawChromosomeRow(graphics, tree, TreeChromosomes.EFFECT);

					textLayout.endPage(graphics);
				}
			});
		}
	}

	@Override
	public void drawAnalyticsPage2(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, individual -> {
				if (individual instanceof ITree tree) {
					IGenome genome = tree.getGenome();
					ITreeSpecies primary = tree.getSpecies();
					ITreeSpecies secondary = tree.getInactiveSpecies();
					Component activeFruit = genome.getActiveName(TreeChromosomes.FRUIT);
					Component inactiveFruit = genome.getInactiveName(TreeChromosomes.FRUIT);

					TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

					textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

					int speciesDominance0 = guiAlyzer.getColorCoding(primary.isDominant());
					int speciesDominance1 = guiAlyzer.getColorCoding(secondary.isDominant());

					textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
					textLayout.drawLine(graphics, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

					textLayout.newLine();
					textLayout.newLine();

					Component yes = Component.translatable("for.yes");
					Component no = Component.translatable("for.no");

					Component fireproofActive = genome.getActiveValue(TreeChromosomes.FIREPROOF) ? yes : no;
					Component fireproofInactive = genome.getInactiveValue(TreeChromosomes.FIREPROOF) ? yes : no;

					guiAlyzer.drawRow(graphics, TreeChromosomes.FIREPROOF.getChromosomeDisplayName(), fireproofActive, fireproofInactive, tree, TreeChromosomes.FIREPROOF);
					textLayout.newLine();
					guiAlyzer.drawRow(graphics, TreeChromosomes.FRUIT.getChromosomeDisplayName(), activeFruit, inactiveFruit, tree, TreeChromosomes.FRUIT);

					textLayout.endPage(graphics);
				}
			});
		}
	}

	@Override
	public void drawAnalyticsPage3(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, individual -> {
				if (individual instanceof ITree tree) {
					TextLayoutHelper textLayout = guiAlyzer.getTextLayout();
					WidgetManager widgetManager = guiAlyzer.getWidgetManager();

					textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

					textLayout.drawLine(graphics, Component.translatable("for.gui.beealyzer.produce").append(":"), GuiAlyzer.COLUMN_0);
					textLayout.newLine();

					int x = GuiAlyzer.COLUMN_0;
					for (IProduct product : tree.getProducts()) {
						widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), product.createStack()));
						x += 18;
						if (x > 148) {
							x = GuiAlyzer.COLUMN_0;
							textLayout.newLine();
						}
					}

					textLayout.newLine();
					textLayout.newLine();
					textLayout.newLine();
					textLayout.newLine();

					textLayout.drawLine(graphics, Component.translatable("for.gui.beealyzer.specialty").append(":"), GuiAlyzer.COLUMN_0);
					textLayout.newLine();

					x = GuiAlyzer.COLUMN_0;
					for (IProduct product : tree.getProducts()) {
						widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), product.createStack()));
						x += 18;
						if (x > 148) {
							x = GuiAlyzer.COLUMN_0;
							textLayout.newLine();
						}
					}

					textLayout.endPage(graphics);
				}
			});
		}
	}

	@Override
	public Map<ISpecies<?>, ItemStack> getIconStacks() {
		return this.iconStacks;
	}

	@Override
	public List<String> getHints() {
		return GuiForestry.HINTS.get("treealyzer");
	}
}
