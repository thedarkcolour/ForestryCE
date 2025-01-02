package forestry.apiculture.genetics;

import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.IIntegerAllele;
import forestry.api.genetics.alleles.IValueAllele;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.config.ForestryConfig;
import forestry.core.gui.GuiAlyzer;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.TextLayoutHelper;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.render.ColourProperties;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;

public enum BeeAlyzerPlugin implements IAlyzerPlugin {
	INSTANCE;

	// todo reloadable
	private final Map<ISpecies<?>, ItemStack> iconStacks = GeneticsUtil.getIconStacks(BeeLifeStage.DRONE, SpeciesUtil.BEE_TYPE.get());

	@Override
	public void drawAnalyticsPage1(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, (individual, stage) -> {
				if (individual instanceof IBee bee) {
					if (ForestryConfig.SERVER.useHaploidDrones.get() && stage == BeeLifeStage.DRONE) {
						TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

						textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

						textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);

						textLayout.newLine();
						textLayout.newLine();

						guiAlyzer.drawHaploidSpeciesRow(graphics, bee, BeeChromosomes.SPECIES);
						textLayout.newLine();

						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.LIFESPAN);
						textLayout.newLine();
						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.SPEED);
						textLayout.newLine();
						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.POLLINATION);
						textLayout.newLine();
						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.FLOWER_TYPE);
						textLayout.newLine();

						textLayout.drawLine(graphics, BeeChromosomes.FERTILITY.getChromosomeDisplayName(), GuiAlyzer.COLUMN_0);
						IIntegerAllele primaryFertility = bee.getGenome().getActiveAllele(BeeChromosomes.FERTILITY);
						guiAlyzer.drawFertilityInfo(graphics, primaryFertility.value(), GuiAlyzer.COLUMN_1, GuiAlyzer.getColorCoding(primaryFertility.dominant()), 0);
						textLayout.newLine();

						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.TERRITORY);
						textLayout.newLine();

						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.EFFECT);
						textLayout.newLine();

						textLayout.endPage(graphics);
					} else {
						TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

						textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

						textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
						textLayout.drawLine(graphics, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

						textLayout.newLine();
						textLayout.newLine();

						guiAlyzer.drawSpeciesRow(graphics, bee, BeeChromosomes.SPECIES);
						textLayout.newLine();

						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.LIFESPAN);
						textLayout.newLine();
						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.SPEED);
						textLayout.newLine();
						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.POLLINATION);
						textLayout.newLine();
						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.FLOWER_TYPE);
						textLayout.newLine();

						textLayout.drawLine(graphics, BeeChromosomes.FERTILITY.getChromosomeDisplayName(), GuiAlyzer.COLUMN_0);
						IIntegerAllele primaryFertility = bee.getGenome().getActiveAllele(BeeChromosomes.FERTILITY);
						IIntegerAllele secondaryFertility = bee.getGenome().getInactiveAllele(BeeChromosomes.FERTILITY);
						guiAlyzer.drawFertilityInfo(graphics, primaryFertility.value(), GuiAlyzer.COLUMN_1, GuiAlyzer.getColorCoding(primaryFertility.dominant()), 0);
						guiAlyzer.drawFertilityInfo(graphics, secondaryFertility.value(), GuiAlyzer.COLUMN_2, GuiAlyzer.getColorCoding(secondaryFertility.dominant()), 0);
						textLayout.newLine();

						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.TERRITORY);
						textLayout.newLine();

						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.EFFECT);
						textLayout.newLine();

						textLayout.endPage(graphics);
					}
				}
			});
		}
	}

	@Override
	public void drawAnalyticsPage2(GuiGraphics graphics, Screen gui, ItemStack stack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(stack, (individual, type) -> {
				if (individual instanceof IBee bee) {
					if (ForestryConfig.SERVER.useHaploidDrones.get() && type == BeeLifeStage.DRONE) {
						IGenome genome = bee.getGenome();
						IBeeSpecies primaryAllele = genome.getActiveValue(BeeChromosomes.SPECIES);

						TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

						textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

						textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);

						textLayout.newLine();

						textLayout.drawRow(graphics, Component.translatable("for.gui.climate"), ClimateHelper.toDisplay(primaryAllele.getTemperature()),
								ColourProperties.INSTANCE.get("gui.screen"), GuiAlyzer.getColorCoding(genome.getActiveAllele(BeeChromosomes.SPECIES).dominant()));

						textLayout.newLine();

						IValueAllele<ToleranceType> tempToleranceActive = genome.getActiveAllele(BeeChromosomes.TEMPERATURE_TOLERANCE);
						textLayout.drawLine(graphics, Component.literal("  ").append(Component.translatable("for.gui.tolerance")), GuiAlyzer.COLUMN_0);
						guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, tempToleranceActive, GuiAlyzer.COLUMN_1);

						textLayout.newLine(16);

						textLayout.drawRow(graphics, Component.translatable("for.gui.humidity"), ClimateHelper.toDisplay(primaryAllele.getHumidity()),
								ColourProperties.INSTANCE.get("gui.screen"), GuiAlyzer.getColorCoding(individual.getGenome().getActiveAllele(BeeChromosomes.SPECIES).dominant()));

						textLayout.newLine();

						IValueAllele<ToleranceType> humidToleranceActive = genome.getActiveAllele(BeeChromosomes.HUMIDITY_TOLERANCE);
						textLayout.drawLine(graphics, Component.literal("  ").append(Component.translatable("for.gui.tolerance")), GuiAlyzer.COLUMN_0);
						guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.HUMIDITY_TOLERANCE, humidToleranceActive, GuiAlyzer.COLUMN_1);

						textLayout.newLine(16);

						Component yes = Component.translatable("for.yes");
						Component no = Component.translatable("for.no");

						guiAlyzer.drawHaploidChromosomeRow(graphics, bee, BeeChromosomes.ACTIVITY);
						textLayout.newLineCompressed();

						Component primary = genome.getActiveValue(BeeChromosomes.TOLERATES_RAIN) ? yes : no;

						textLayout.drawLine(graphics, Component.translatable("chromosome.forestry.tolerates_rain"), GuiAlyzer.COLUMN_0);
						textLayout.drawLine(graphics, primary, GuiAlyzer.COLUMN_1, GuiAlyzer.getColorCoding(false));
						textLayout.newLineCompressed();

						primary = genome.getActiveValue(BeeChromosomes.CAVE_DWELLING) ? yes : no;

						textLayout.drawLine(graphics, Component.translatable("for.gui.cave"), GuiAlyzer.COLUMN_0);
						textLayout.drawLine(graphics, primary, GuiAlyzer.COLUMN_1, GuiAlyzer.getColorCoding(false));

						textLayout.newLine();

						textLayout.endPage(graphics);
					} else {
						IGenome genome = bee.getGenome();
						IBeeSpecies primaryAllele = genome.getActiveValue(BeeChromosomes.SPECIES);
						IBeeSpecies secondaryAllele = genome.getInactiveValue(BeeChromosomes.SPECIES);

						TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

						textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

						textLayout.drawLine(graphics, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
						textLayout.drawLine(graphics, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

						textLayout.newLine();

						guiAlyzer.drawRow(graphics, Component.translatable("for.gui.climate"), ClimateHelper.toDisplay(primaryAllele.getTemperature()),
								ClimateHelper.toDisplay(secondaryAllele.getTemperature()), bee, BeeChromosomes.SPECIES);

						textLayout.newLine();

						IValueAllele<ToleranceType> tempToleranceActive = genome.getActiveAllele(BeeChromosomes.TEMPERATURE_TOLERANCE);
						IValueAllele<ToleranceType> tempToleranceInactive = genome.getInactiveAllele(BeeChromosomes.TEMPERATURE_TOLERANCE);
						textLayout.drawLine(graphics, Component.literal("  ").append(Component.translatable("for.gui.tolerance")), GuiAlyzer.COLUMN_0);
						guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, tempToleranceActive, GuiAlyzer.COLUMN_1);
						guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, tempToleranceInactive, GuiAlyzer.COLUMN_2);

						textLayout.newLine(16);

						guiAlyzer.drawRow(graphics, Component.translatable("for.gui.humidity"), ClimateHelper.toDisplay(primaryAllele.getHumidity()),
								ClimateHelper.toDisplay(secondaryAllele.getHumidity()), bee, BeeChromosomes.SPECIES);

						textLayout.newLine();

						IValueAllele<ToleranceType> humidToleranceActive = genome.getActiveAllele(BeeChromosomes.HUMIDITY_TOLERANCE);
						IValueAllele<ToleranceType> humidToleranceInactive = genome.getInactiveAllele(BeeChromosomes.HUMIDITY_TOLERANCE);
						textLayout.drawLine(graphics, Component.literal("  ").append(Component.translatable("for.gui.tolerance")), GuiAlyzer.COLUMN_0);
						guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, humidToleranceActive, GuiAlyzer.COLUMN_1);
						guiAlyzer.drawToleranceInfo(graphics, BeeChromosomes.TEMPERATURE_TOLERANCE, humidToleranceInactive, GuiAlyzer.COLUMN_2);

						textLayout.newLine(16);

						Component yes = Component.translatable("for.yes");
						Component no = Component.translatable("for.no");

						guiAlyzer.drawChromosomeRow(graphics, bee, BeeChromosomes.ACTIVITY);
						textLayout.newLineCompressed();

						Component primary = genome.getActiveValue(BeeChromosomes.TOLERATES_RAIN) ? yes : no;
						Component secondary = genome.getInactiveValue(BeeChromosomes.TOLERATES_RAIN) ? yes : no;

						textLayout.drawLine(graphics, Component.translatable("chromosome.forestry.tolerates_rain"), GuiAlyzer.COLUMN_0);
						textLayout.drawLine(graphics, primary, GuiAlyzer.COLUMN_1, GuiAlyzer.getColorCoding(false));
						textLayout.drawLine(graphics, secondary, GuiAlyzer.COLUMN_2, GuiAlyzer.getColorCoding(false));

						textLayout.newLineCompressed();

						primary = genome.getActiveValue(BeeChromosomes.CAVE_DWELLING) ? yes : no;
						secondary = genome.getInactiveValue(BeeChromosomes.CAVE_DWELLING) ? yes : no;

						textLayout.drawLine(graphics, Component.translatable("for.gui.cave"), GuiAlyzer.COLUMN_0);
						textLayout.drawLine(graphics, primary, GuiAlyzer.COLUMN_1, GuiAlyzer.getColorCoding(false));
						textLayout.drawLine(graphics, secondary, GuiAlyzer.COLUMN_2, GuiAlyzer.getColorCoding(false));

						textLayout.newLine();

						if (type == BeeLifeStage.PRINCESS || type == BeeLifeStage.QUEEN) {
							String displayTextKey = "for.bees.stock.pristine";
							if (!bee.isPristine()) {
								displayTextKey = "for.bees.stock.ignoble";
							}
							Component displayText = Component.translatable(displayTextKey);
							textLayout.drawCenteredLine(graphics, displayText, 8, 208, guiAlyzer.getFontColor().get("gui.beealyzer.binomial"));
						}

						if (bee.getGeneration() > 0) {
							textLayout.newLineCompressed();
							Component displayText = Component.translatable("for.gui.beealyzer.generations", bee.getGeneration());
							textLayout.drawCenteredLine(graphics, displayText, 8, 208, guiAlyzer.getFontColor().get("gui.beealyzer.binomial"));
						}

						textLayout.endPage(graphics);
					}
				}
			});
		}
	}

	@Override
	public void drawAnalyticsPage3(GuiGraphics graphics, Screen gui, ItemStack itemStack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IIndividualHandlerItem.ifPresent(itemStack, individual -> {
				if (individual instanceof IBee bee) {
					TextLayoutHelper textLayout = guiAlyzer.getTextLayout();
					WidgetManager widgetManager = guiAlyzer.getWidgetManager();

					textLayout.startPage(graphics, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

					textLayout.drawLine(graphics, Component.translatable("for.gui.beealyzer.produce").append(":"), GuiAlyzer.COLUMN_0);

					textLayout.newLine();

					int x = GuiAlyzer.COLUMN_0;
					for (ItemStack stack : bee.getProduceList()) {
						widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), stack));

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
					for (ItemStack stack : bee.getSpecialtyList()) {
						widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), stack));

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
		return GuiForestry.HINTS.get("beealyzer");
	}

}
