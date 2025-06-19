package forestry.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.*;
import forestry.api.genetics.alleles.*;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.config.Constants;
import forestry.core.genetics.mutations.EnumMutateChance;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.inventory.PortableAnalyzerInventory;
import forestry.core.render.ColourProperties;
import forestry.core.utils.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Portable analyzer
public class PortableAnalyzerScreen extends GuiForestry<PortableAnalyzerMenu> {
	public static final int COLUMN_0 = 12;
	public static final int COLUMN_1 = 90;
	public static final int COLUMN_2 = 155;

	private final PortableAnalyzerInventory itemInventory;

	public PortableAnalyzerScreen(PortableAnalyzerMenu container, Inventory playerInv, Component name) {
		super(Constants.TEXTURE_PATH_GUI + "/portablealyzer.png", container, playerInv, name);

		this.itemInventory = container.inventory;
		this.imageWidth = 247;
		this.imageHeight = 238;
	}

	public static int getColorCoding(boolean dominant) {
		if (dominant) {
			return ColourProperties.INSTANCE.get("gui.beealyzer.dominant");
		} else {
			return ColourProperties.INSTANCE.get("gui.beealyzer.recessive");
		}
	}

	public void drawLine(GuiGraphics graphics, String text, int x, IIndividual individual, IChromosome<?> chromosome, boolean inactive) {
		if (!inactive) {
            this.textLayout.drawLine(graphics, text, x, getColorCoding(individual.getGenome().getActiveAllele(chromosome).dominant()));
		} else {
            this.textLayout.drawLine(graphics, text, x, getColorCoding(individual.getGenome().getInactiveAllele(chromosome).dominant()));
		}
	}

	public void drawSplitLine(GuiGraphics graphics, Component component, int x, int maxWidth, IIndividual individual, IChromosome<?> chromosome, boolean inactive) {
		if (!inactive) {
            this.textLayout.drawSplitLine(graphics, component, x, maxWidth, getColorCoding(individual.getGenome().getActiveAllele(chromosome).dominant()));
		} else {
            this.textLayout.drawSplitLine(graphics, component, x, maxWidth, getColorCoding(individual.getGenome().getInactiveAllele(chromosome).dominant()));
		}
	}

	public void drawRow(GuiGraphics graphics, Component text0, Component text1, Component text2, IIndividual individual, IChromosome<?> chromosome) {
        this.textLayout.drawRow(graphics, text0, text1, text2, ColourProperties.INSTANCE.get("gui.screen"), getColorCoding(individual.getGenome().getActiveAllele(chromosome).dominant()),
			getColorCoding(individual.getGenome().getInactiveAllele(chromosome).dominant()));
	}

	public void drawChromosomeRow(GuiGraphics graphics, IIndividual individual, IChromosome<?> chromosome) {
		IAllele active = individual.getGenome().getActiveAllele(chromosome);
		MutableComponent activeName = chromosome.getDisplayName(active.cast());
		IAllele inactive = individual.getGenome().getInactiveAllele(chromosome);
		MutableComponent inactiveName = chromosome.getDisplayName(inactive.cast());
        this.textLayout.drawRow(graphics, chromosome.getChromosomeDisplayName(), activeName, inactiveName, ColourProperties.INSTANCE.get("gui.screen"), getColorCoding(active.dominant()), getColorCoding(inactive.dominant()));
	}

	public void drawHaploidChromosomeRow(GuiGraphics graphics, IIndividual individual, IChromosome<?> chromosome) {
		IAllele active = individual.getGenome().getActiveAllele(chromosome);
		MutableComponent activeName = chromosome.getDisplayName(active.cast());
        this.textLayout.drawRow(graphics, chromosome.getChromosomeDisplayName(), activeName, ColourProperties.INSTANCE.get("gui.screen"), getColorCoding(active.dominant()));
	}

	public <S extends ISpecies<?>> void drawSpeciesRow(GuiGraphics graphics, IIndividual individual, IRegistryChromosome<S> chromosome) {
		AllelePair<IValueAllele<? extends S>> species = individual.getGenome().getAllelePair(chromosome);

        this.textLayout.drawLine(graphics, chromosome.getChromosomeDisplayName(), this.textLayout.column0);
		int columnwidth = this.textLayout.column2 - this.textLayout.column1 - 2;

		IValueAllele<? extends S> activeSpecies = species.active();
		IValueAllele<? extends S> inactiveSpecies = species.inactive();

		Map<ISpecies<?>, ItemStack> iconStacks = activeSpecies.value().getType().getAlyzerPlugin().getIconStacks();

		// todo fix Icon Stacks being empty for butterflies
		GuiUtil.drawItemStack(graphics, this, iconStacks.get(activeSpecies.value()), this.leftPos + this.textLayout.column1 + columnwidth - 20, this.topPos + 10);
		GuiUtil.drawItemStack(graphics, this, iconStacks.get(inactiveSpecies.value()), this.leftPos + this.textLayout.column2 + columnwidth - 20, this.topPos + 10);

		Component primaryName = chromosome.getDisplayName(activeSpecies);
		Component secondaryName = chromosome.getDisplayName(inactiveSpecies);

		drawSplitLine(graphics, primaryName, this.textLayout.column1, columnwidth, individual, chromosome, false);
		drawSplitLine(graphics, secondaryName, this.textLayout.column2, columnwidth, individual, chromosome, true);

        this.textLayout.newLine();
	}

	public <S extends ISpecies<?>> void drawHaploidSpeciesRow(GuiGraphics graphics, IIndividual individual, IRegistryChromosome<S> chromosome) {
		AllelePair<IValueAllele<? extends S>> species = individual.getGenome().getAllelePair(chromosome);

        this.textLayout.drawLine(graphics, chromosome.getChromosomeDisplayName(), this.textLayout.column0);
		int columnwidth = this.textLayout.column2 - this.textLayout.column1 - 2;

		IValueAllele<? extends S> activeSpecies = species.active();
		Map<ISpecies<?>, ItemStack> iconStacks = activeSpecies.value().getType().getAlyzerPlugin().getIconStacks();
		// todo fix Icon Stacks being empty for butterflies
		GuiUtil.drawItemStack(graphics, this, iconStacks.get(activeSpecies.value()), this.leftPos + this.textLayout.column1 + columnwidth - 20, this.topPos + 10);
		Component primaryName = chromosome.getDisplayName(activeSpecies);
		drawSplitLine(graphics, primaryName, this.textLayout.column1, columnwidth, individual, chromosome, false);
        this.textLayout.newLine();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);
        this.widgetManager.clear();

		int specimenSlot = getSpecimenSlot();
		if (specimenSlot < PortableAnalyzerInventory.SLOT_ANALYZE_1) {
			drawAnalyticsOverview(graphics);
			return;
		}

		ItemStack stackInSlot = this.itemInventory.getItem(specimenSlot);

		IIndividualHandlerItem.ifPresent(stackInSlot, individual -> {
			ISpeciesType<?, ?> type = individual.getType();

			switch (specimenSlot) {
				case PortableAnalyzerInventory.SLOT_ANALYZE_1 ->
					type.getAlyzerPlugin().drawAnalyticsPage1(graphics, this, stackInSlot);
				case PortableAnalyzerInventory.SLOT_ANALYZE_2 ->
					type.getAlyzerPlugin().drawAnalyticsPage2(graphics, this, stackInSlot);
				case PortableAnalyzerInventory.SLOT_ANALYZE_3 ->
					type.getAlyzerPlugin().drawAnalyticsPage3(graphics, this, stackInSlot);
				case PortableAnalyzerInventory.SLOT_ANALYZE_4 -> drawAnalyticsPageMutations(graphics, individual);
				case PortableAnalyzerInventory.SLOT_ANALYZE_5 -> drawAnalyticsPageClassification(graphics, individual);
				default -> drawAnalyticsOverview(graphics);
			}
		});
	}

	private int getSpecimenSlot() {
		for (int k = PortableAnalyzerInventory.SLOT_SPECIMEN; k <= PortableAnalyzerInventory.SLOT_ANALYZE_5; k++) {
			ItemStack stackInSlot = this.itemInventory.getItem(k);

			if (!stackInSlot.isEmpty() && IIndividualHandlerItem.filter(stackInSlot, IIndividual::isAnalyzed)) {
				return k;
			}
		}
		return -1;
	}

	public void drawAnalyticsOverview(GuiGraphics graphics) {
        this.textLayout.startPage(graphics);

        this.textLayout.newLine();
		Component title = Component.translatable("for.gui.portablealyzer");
        this.textLayout.drawCenteredLine(graphics, title, 8, 208, ColourProperties.INSTANCE.get("gui.screen"));
        this.textLayout.newLine();

		graphics.drawWordWrap(this.font, Component.translatable("for.gui.portablealyzer.help"), this.leftPos + COLUMN_0 + 4, this.topPos + 42, 200, ColourProperties.INSTANCE.get("gui.screen"));
        this.textLayout.newLine();
        this.textLayout.newLine();
        this.textLayout.newLine();
        this.textLayout.newLine();

        this.textLayout.drawLine(graphics, Component.translatable("for.gui.alyzer.overview").append(":"), COLUMN_0 + 4);
        this.textLayout.newLine();
        this.textLayout.drawLine(graphics, Component.literal("I  : ").append(Component.translatable("for.gui.general")), COLUMN_0 + 4);
        this.textLayout.newLine();
        this.textLayout.drawLine(graphics, Component.literal("II : ").append(Component.translatable("for.gui.environment")), COLUMN_0 + 4);
        this.textLayout.newLine();
        this.textLayout.drawLine(graphics, Component.literal("III: ").append(Component.translatable("for.gui.produce")), COLUMN_0 + 4);
        this.textLayout.newLine();
        this.textLayout.drawLine(graphics, Component.literal("IV : ").append(Component.translatable("for.gui.evolution")), COLUMN_0 + 4);

        this.textLayout.endPage(graphics);
	}

	public void drawAnalyticsPageClassification(GuiGraphics graphics, IIndividual individual) {
        this.textLayout.startPage(graphics);

        this.textLayout.drawLine(graphics, Component.translatable("for.gui.alyzer.classification").append(":"), 12);
        this.textLayout.newLine();

		ArrayDeque<ITaxon> hierarchy = new ArrayDeque<>();
		ISpecies<?> species = individual.getSpecies();
		ITaxon taxon = species.getGenus();
		while (taxon != null) {
			if (!taxon.name().isEmpty()) {
				hierarchy.push(taxon);
			}
			taxon = taxon.parent();
		}

		boolean overcrowded = hierarchy.size() > 5;
		int x = 12;
		ITaxon group;

		while (!hierarchy.isEmpty()) {
			group = hierarchy.pop();
			if (overcrowded && group.rank().isDroppable()) {
				continue;
			}

			String name = Character.toUpperCase(group.name().charAt(0)) + group.name().substring(1);
            this.textLayout.drawLine(graphics, name, x, group.rank().getColour());
            this.textLayout.drawLine(graphics, group.rank().name(), 170, group.rank().getColour());
            this.textLayout.newLineCompressed();
			x += 12;
		}

		// Add the species name
		String binomial = species.getBinomial();
		if (this.font.width(binomial) > 96) {
			binomial = Character.toUpperCase(species.getGenusName().charAt(0)) + ". " + species.getSpeciesName();
		}

        this.textLayout.drawLine(graphics, binomial, x, 0xebae85);
        this.textLayout.drawLine(graphics, "SPECIES", 170, 0xebae85);

        this.textLayout.newLine();
        this.textLayout.drawLine(graphics, Component.translatable("for.gui.alyzer.authority").append(": ").append(species.getAuthority()), 12);

        this.textLayout.newLine();
		String description = species.getDescriptionTranslationKey();
		if (Translator.canTranslateToLocal(description)) {
			description = Component.translatable(description).getString();
			String[] tokens = description.split("\\|");
            this.textLayout.drawSplitLine(graphics, tokens[0], 12, 200, 0x666666);
			if (tokens.length > 1) {
				String signature = "- " + tokens[1];
				graphics.drawString(this.font, signature, this.leftPos + 210 - font().width(signature), this.topPos + 145 - 10, 0x99cc32, true);
			}
		} else {
            this.textLayout.drawSplitLine(graphics, Component.translatable("for.gui.alyzer.nodescription"), 12, 200, 0x666666);
		}

        this.textLayout.endPage(graphics);
	}

	public <I extends IIndividual> void drawAnalyticsPageMutations(GuiGraphics graphics, I individual) {
        this.textLayout.startPage(graphics, COLUMN_0, COLUMN_1, COLUMN_2);
        this.textLayout.drawLine(graphics, Component.translatable("for.gui.beealyzer.mutations").append(":"), COLUMN_0);
        this.textLayout.newLine();

		ISpeciesType<?, ?> speciesRoot = individual.getType();
		ISpecies<?> species = individual.getSpecies();

		int columnWidth = 50;
		int x = 0;

		Player player = Minecraft.getInstance().player;
		IBreedingTracker breedingTracker = speciesRoot.getBreedingTracker(player.level(), player.getGameProfile());

		IMutationManager<?> mutations = species.getType().getMutations();
		for (IMutation<?> mutation : mutations.getMutationsFrom(species.cast())) {
			if (breedingTracker.isDiscovered(mutation)) {
				drawMutationInfo(graphics, mutation, species, COLUMN_0 + x, breedingTracker);
			} else {
				// Do not display secret undiscovered mutations.
				if (mutation.isSecret()) {
					continue;
				}

				drawUnknownMutation(graphics, mutation, COLUMN_0 + x, breedingTracker);
			}

			x += columnWidth;
			if (x >= columnWidth * 4) {
				x = 0;
                this.textLayout.newLine(16);
			}
		}

        this.textLayout.endPage(graphics);
	}

	public void drawMutationInfo(GuiGraphics graphics, IMutation<?> combination, ISpecies<?> species, int x, IBreedingTracker breedingTracker) {
		Map<ISpecies<?>, ItemStack> iconStacks = combination.getType().getAlyzerPlugin().getIconStacks();

		ItemStack partnerBee = iconStacks.get(combination.getPartner(species));
        this.widgetManager.add(new ItemStackWidget(this.widgetManager, x, this.textLayout.getLineY(), partnerBee));

		drawProbabilityArrow(graphics, combination, this.leftPos + x + 18, this.topPos + this.textLayout.getLineY() + 4, breedingTracker);

		ISpecies<?> result = combination.getResult();
		ItemStack resultBee = iconStacks.get(result);
        this.widgetManager.add(new ItemStackWidget(this.widgetManager, x + 33, this.textLayout.getLineY(), resultBee));
	}

	private void drawUnknownMutation(GuiGraphics graphics, IMutation<?> combination, int x, IBreedingTracker breedingTracker) {
		drawQuestionMark(graphics, this.leftPos + x, this.topPos + this.textLayout.getLineY());
		drawProbabilityArrow(graphics, combination, this.leftPos + x + 18, this.topPos + this.textLayout.getLineY() + 4, breedingTracker);
		drawQuestionMark(graphics, this.leftPos + x + 32, this.topPos + this.textLayout.getLineY());
	}

	private void drawQuestionMark(GuiGraphics graphics, int x, int y) {
		graphics.blit(this.textureFile, x, y, 78, 240, 16, 16);
	}

	private void drawProbabilityArrow(GuiGraphics graphics, IMutation<?> combination, int x, int y, IBreedingTracker breedingTracker) {
		float chance = combination.getChance();
		int line = 247;
		int column = switch (EnumMutateChance.rateChance(chance)) {
			case HIGHER -> 100 + 15;
			case HIGH -> 100 + 15 * 2;
			case NORMAL -> 100 + 15 * 3;
			case LOW -> 100 + 15 * 4;
			case LOWEST -> 100 + 15 * 5;
			default -> 100;
		};

		// Probability arrow
		graphics.blit(this.textureFile, x, y, column, line, 15, 9);

		boolean researched = breedingTracker.isResearched(combination);
		if (researched) {
			graphics.drawString(this.font, "+", x + 9, y + 1, 0, false);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	public void drawToleranceInfo(GuiGraphics graphics, IValueChromosome<ToleranceType> chromosome, IValueAllele<? extends ToleranceType> toleranceAllele, int x) {
		int textColor = getColorCoding(toleranceAllele.dominant());
		ToleranceType tolerance = toleranceAllele.value();
		Component text = Component.literal("(").append(chromosome.getDisplayName(toleranceAllele)).append(")");

		// Enable correct lighting.
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		switch (tolerance) {
			case BOTH_1, BOTH_2, BOTH_3, BOTH_4, BOTH_5 -> {
				drawBothSymbol(graphics, x - 2, this.textLayout.getLineY() - 1);
                this.textLayout.drawLine(graphics, text, x + 14, textColor);
			}
			case DOWN_1, DOWN_2, DOWN_3, DOWN_4, DOWN_5 -> {
				drawDownSymbol(graphics, x - 2, this.textLayout.getLineY() - 1);
                this.textLayout.drawLine(graphics, text, x + 14, textColor);
			}
			case UP_1, UP_2, UP_3, UP_4, UP_5 -> {
				drawUpSymbol(graphics, x - 2, this.textLayout.getLineY() - 1);
                this.textLayout.drawLine(graphics, text, x + 14, textColor);
			}
			default -> {
				drawNoneSymbol(graphics, x - 2, this.textLayout.getLineY() - 1);
                this.textLayout.drawLine(graphics, "(0)", x + 14, textColor);
			}
		}
	}

	private void drawDownSymbol(GuiGraphics graphics, int x, int y) {
		graphics.blit(this.textureFile, this.leftPos + x, this.topPos + y, 0, 247, 15, 9);
	}

	private void drawUpSymbol(GuiGraphics graphics, int x, int y) {
		graphics.blit(this.textureFile, this.leftPos + x, this.topPos + y, 15, 247, 15, 9);
	}

	private void drawBothSymbol(GuiGraphics graphics, int x, int y) {
		graphics.blit(this.textureFile, this.leftPos + x, this.topPos + y, 30, 247, 15, 9);
	}

	private void drawNoneSymbol(GuiGraphics graphics, int x, int y) {
		graphics.blit(this.textureFile, this.leftPos + x, this.topPos + y, 45, 247, 15, 9);
	}

	public void drawFertilityInfo(GuiGraphics graphics, int fertility, int x, int textColor, int texOffset) {
		// Enable correct lighting.
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		String fertilityString = fertility + " x";

		int stringWidth = font().width(fertilityString);

		graphics.blit(this.textureFile, this.leftPos + x + stringWidth + 2, this.topPos + this.textLayout.getLineY() - 1, 60, 240 + texOffset, 12, 8);

		this.textLayout.drawLine(graphics, fertilityString, x, textColor);
	}

	public WidgetManager getWidgetManager() {
		return this.widgetManager;
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.itemInventory);
		addHintLedger(getHints());
	}

	public List<String> getHints() {
		ItemStack specimen = this.itemInventory.getSpecimen();
		if (!specimen.isEmpty()) {
			IIndividual individual = IIndividualHandlerItem.getIndividual(specimen);
			if (individual != null) {
				return individual.getType().getAlyzerPlugin().getHints();
			}
		}
		return Collections.emptyList();
	}
}
