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
package forestry.lepidopterology.genetics;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.core.IProduct;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.alleles.IIntegerAllele;
import forestry.api.genetics.alleles.IValueAllele;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.core.gui.GuiAlyzer;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.TextLayoutHelper;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;

public enum ButterflyAlyzerPlugin implements IAlyzerPlugin {
	INSTANCE;

	// todo reloadable
	private final IdentityHashMap<ISpecies<?>, ItemStack> iconStacks = GeneticsUtil.getIconStacks(ButterflyLifeStage.BUTTERFLY, SpeciesUtil.BUTTERFLY_TYPE.get());

	@Override
	public void drawAnalyticsPage1(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, (butterfly, stage) -> {
				IGenome genome = butterfly.getGenome();

				TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

				textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

				textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
				textLayout.drawLine(graphics, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

				textLayout.newLine();
				textLayout.newLine();

				guiAlyzer.drawSpeciesRow(graphics, butterfly, ButterflyChromosomes.SPECIES);
				textLayout.newLine();

				guiAlyzer.drawChromosomeRow(graphics, butterfly, ButterflyChromosomes.SIZE);
				textLayout.newLine();

				//guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.lifespan"), butterfly, ButterflyChromosomes.LIFESPAN);
				//textLayout.newLine();

				guiAlyzer.drawChromosomeRow(graphics, butterfly, ButterflyChromosomes.SPEED);
				textLayout.newLine();

				guiAlyzer.drawChromosomeRow(graphics, butterfly, ButterflyChromosomes.METABOLISM);
				textLayout.newLine();

				textLayout.drawLine(graphics, ButterflyChromosomes.FERTILITY.getChromosomeDisplayName(), GuiAlyzer.COLUMN_0);
				AllelePair<IIntegerAllele> fertilityPair = genome.getAllelePair(ButterflyChromosomes.FERTILITY);
				guiAlyzer.drawFertilityInfo(graphics, fertilityPair.active().value(), GuiAlyzer.COLUMN_1, guiAlyzer.getColorCoding(fertilityPair.active().dominant()), 8);
				guiAlyzer.drawFertilityInfo(graphics, fertilityPair.inactive().value(), GuiAlyzer.COLUMN_2, guiAlyzer.getColorCoding(fertilityPair.inactive().dominant()), 8);
				textLayout.newLine();

				guiAlyzer.drawChromosomeRow(graphics, butterfly, ButterflyChromosomes.FLOWER_TYPE);
				textLayout.newLine();

				guiAlyzer.drawChromosomeRow(graphics, butterfly, ButterflyChromosomes.EFFECT);
				textLayout.newLine();

				textLayout.endPage(graphics);
			});
		}
	}

	@Override
	public void drawAnalyticsPage2(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, (individual, stage) -> {
				IGenome genome = individual.getGenome();
				IButterflySpecies primaryAllele = genome.getActiveValue(ButterflyChromosomes.SPECIES);
				IButterflySpecies secondaryAllele = genome.getActiveValue(ButterflyChromosomes.SPECIES);

				TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

				textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

				textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
				textLayout.drawLine(graphics, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

				textLayout.newLine();
				textLayout.newLine();

				guiAlyzer.drawRow(graphics, ButterflyChromosomes.TEMPERATURE_TOLERANCE.getChromosomeDisplayName(),
						ClimateHelper.toDisplay(primaryAllele.getTemperature()),
						ClimateHelper.toDisplay(secondaryAllele.getTemperature()), individual, ButterflyChromosomes.SPECIES);
				textLayout.newLine();

				Component indentedTolerance = Component.literal("  ").append(Component.translatable("for.gui.tolerance"));
				IValueAllele<ToleranceType> tempToleranceActive = genome.getActiveAllele(ButterflyChromosomes.TEMPERATURE_TOLERANCE);
				IValueAllele<ToleranceType> tempToleranceInactive = genome.getInactiveAllele(ButterflyChromosomes.TEMPERATURE_TOLERANCE);

				textLayout.drawLine(graphics, indentedTolerance, GuiAlyzer.COLUMN_0);
				guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, tempToleranceActive, GuiAlyzer.COLUMN_1);
				guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, tempToleranceInactive, GuiAlyzer.COLUMN_2);

				textLayout.newLine();

				guiAlyzer.drawRow(graphics, ButterflyChromosomes.HUMIDITY_TOLERANCE.getChromosomeDisplayName(),
						ClimateHelper.toDisplay(primaryAllele.getHumidity()),
						ClimateHelper.toDisplay(secondaryAllele.getHumidity()), individual, ButterflyChromosomes.SPECIES);
				textLayout.newLine();

				IValueAllele<ToleranceType> humidToleranceActive = genome.getActiveAllele(ButterflyChromosomes.HUMIDITY_TOLERANCE);
				IValueAllele<ToleranceType> humidToleranceInactive = genome.getInactiveAllele(ButterflyChromosomes.HUMIDITY_TOLERANCE);
				textLayout.drawLine(graphics, indentedTolerance, GuiAlyzer.COLUMN_0);
				guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, humidToleranceActive, GuiAlyzer.COLUMN_1);
				guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, humidToleranceInactive, GuiAlyzer.COLUMN_2);

				textLayout.newLine();
				textLayout.newLine();

				Component yes = Component.translatable("for.yes");
				Component no = Component.translatable("for.no");

				Component diurnal0, diurnal1, nocturnal0, nocturnal1;
				if (genome.getActiveValue(ButterflyChromosomes.NEVER_SLEEPS)) {
					nocturnal0 = diurnal0 = yes;
				} else {
					nocturnal0 = primaryAllele.isNocturnal() ? yes : no;
					diurnal0 = !primaryAllele.isNocturnal() ? yes : no;
				}
				if (genome.getActiveValue(ButterflyChromosomes.NEVER_SLEEPS)) {
					nocturnal1 = diurnal1 = yes;
				} else {
					nocturnal1 = secondaryAllele.isNocturnal() ? yes : no;
					diurnal1 = !secondaryAllele.isNocturnal() ? yes : no;
				}

				textLayout.drawLine(graphics, Component.translatable("for.gui.diurnal"), GuiAlyzer.COLUMN_0);
				textLayout.drawLine(graphics, diurnal0, GuiAlyzer.COLUMN_1, guiAlyzer.getColorCoding(false));
				textLayout.drawLine(graphics, diurnal1, GuiAlyzer.COLUMN_2, guiAlyzer.getColorCoding(false));
				textLayout.newLine();

				textLayout.drawLine(graphics, Component.translatable("for.gui.nocturnal"), GuiAlyzer.COLUMN_0);
				textLayout.drawLine(graphics, nocturnal0, GuiAlyzer.COLUMN_1, guiAlyzer.getColorCoding(false));
				textLayout.drawLine(graphics, nocturnal1, GuiAlyzer.COLUMN_2, guiAlyzer.getColorCoding(false));
				textLayout.newLine();

				Component primary = genome.getActiveValue(ButterflyChromosomes.TOLERATES_RAIN) ? yes : no;
				Component secondary = genome.getInactiveValue(ButterflyChromosomes.TOLERATES_RAIN) ? yes : no;

				guiAlyzer.drawRow(graphics, ButterflyChromosomes.TOLERATES_RAIN.getChromosomeDisplayName(), primary, secondary, individual, ButterflyChromosomes.TOLERATES_RAIN);
				textLayout.newLine();

				primary = genome.getActiveValue(ButterflyChromosomes.FIREPROOF) ? yes : no;
				secondary = genome.getInactiveValue(ButterflyChromosomes.FIREPROOF) ? yes : no;

				guiAlyzer.drawRow(graphics, ButterflyChromosomes.FIREPROOF.getChromosomeDisplayName(), primary, secondary, individual, ButterflyChromosomes.FIREPROOF);

				textLayout.endPage(graphics);
			});
		}
	}

	@Override
	public void drawAnalyticsPage3(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, individual -> {
				IGenome genome = individual.getGenome();
				TextLayoutHelper textLayout = guiAlyzer.getTextLayout();
				WidgetManager widgetManager = guiAlyzer.getWidgetManager();

				textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

				textLayout.drawLine(graphics, Component.translatable("for.gui.loot.butterfly").append(":"), GuiAlyzer.COLUMN_0);
				textLayout.newLine();

				int x = GuiAlyzer.COLUMN_0;

				for (IProduct product : genome.getActiveValue(ButterflyChromosomes.SPECIES).getButterflyLoot()) {
					widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), product.createStack()));
					x += 18;
					if (x > 148) {
						x = GuiAlyzer.COLUMN_0;
						textLayout.newLine();
					}
				}

				textLayout.newLine();
				textLayout.newLine();

				textLayout.drawLine(graphics, Component.translatable("for.gui.loot.caterpillar").append(":"), GuiAlyzer.COLUMN_0);
				textLayout.newLine();

				x = GuiAlyzer.COLUMN_0;
				for (IProduct product : genome.getActiveValue(ButterflyChromosomes.SPECIES).getCaterpillarProducts()) {
					widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), product.createStack()));
					x += 18;
					if (x > 148) {
						x = GuiAlyzer.COLUMN_0;
						textLayout.newLine();
					}
				}

				textLayout.newLine();
				textLayout.newLine();

				textLayout.drawLine(graphics, Component.translatable("for.gui.loot.cocoon").append(":"), GuiAlyzer.COLUMN_0);
				textLayout.newLine();

				x = GuiAlyzer.COLUMN_0;
				for (IProduct product : genome.getActiveValue(ButterflyChromosomes.COCOON).getProducts()) {
					widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), product.createStack()));
					x += 18;
					if (x > 148) {
						x = GuiAlyzer.COLUMN_0;
						textLayout.newLine();
					}
				}

				textLayout.endPage(graphics);
			});
		}
	}

	@Override
	public Map<ISpecies<?>, ItemStack> getIconStacks() {
		return this.iconStacks;
	}

	@Override
	public List<String> getHints() {
		return GuiForestry.HINTS.get("flutterlyzer");
	}
}
