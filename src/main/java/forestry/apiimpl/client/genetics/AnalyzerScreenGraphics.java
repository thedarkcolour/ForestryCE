package forestry.apiimpl.client.genetics;

import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.InteractableTextOptions;
import forestry.api.client.TextOptions;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.core.IClimateSensitive;
import forestry.api.core.IProduct;
import forestry.api.core.Product;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.*;
import forestry.api.genetics.alleles.*;
import forestry.core.ForestryColors;
import forestry.core.genetics.mutations.EnumMutateChance;
import forestry.core.gui.GuiUtil;
import forestry.core.gui.PortableAnalyzerScreen;
import forestry.core.utils.Translator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AnalyzerScreenGraphics<S extends ISpecies<I>, I extends IIndividual> implements IAnalyzerGraphics<S, I> {
	public static final int COLUMN_1 = 78;
	public static final int COLUMN_2 = 143;

	private final GuiGraphics graphics;
	private final Font font;
	private final int mouseX;
	private final int mouseY;
	private final IIndividual individual;
	private final IGenome genome;
	private final ResourceLocation texture;

	private boolean haploid;
	private int currentX;
	private int currentY;

	public AnalyzerScreenGraphics(GuiGraphics graphics, PortableAnalyzerScreen parent, int mouseX, int mouseY, I individual) {
		this.graphics = graphics;
		this.font = parent.getMinecraft().font;
		this.currentX = parent.getGuiLeft() + 12;
		this.currentY = parent.getGuiTop() + 12;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.individual = individual;
		this.genome = individual.getGenome();
		this.texture = parent.textureFile;
	}

	private static FormattedCharSequence withStyle(FormattedCharSequence text, Style style) {
		return formattedCharSink -> text.accept((i, s, j) -> formattedCharSink.accept(i, style, j));
	}

	@Override
	public <C extends IChromosome<A>, A extends IAllele> void drawChromosomeRow(C chromosome, @Nullable IChromosomeRowOptions<C, A> options) {
		// chromosome label
		AllelePair<A> pair = this.genome.getAllelePair(chromosome);
		A active = pair.active();
		A inactive = pair.inactive();

		ISplitLineOptions newOptions = (right, existing, text) -> {
			A allele = right ? inactive : active;
			existing.setColor(PortableAnalyzerScreen.getColorCoding(allele.dominant()));
			return options == null ? text : options.apply(!right, chromosome, allele, existing, text);
		};

		drawSplitLine(chromosome.getChromosomeDisplayName(), chromosome.getDisplayName(active), chromosome.getDisplayName(inactive), !this.haploid, newOptions);
	}

	public void drawSplitLine(Component label, Component left, Component right, boolean showRight, @Nullable ISplitLineOptions options) {
		drawText(label);

		InteractableTextOptions leftOptions = new InteractableTextOptions();
		Component leftText = left;
		if (options != null) {
			leftText = options.apply(false, leftOptions, leftText);
		}
		drawText(leftText, COLUMN_1, leftOptions);

		if (showRight) {
			InteractableTextOptions rightOptions = new InteractableTextOptions();
			Component rightText = right;
			if (options != null) {
				rightText = options.apply(true, rightOptions, rightText);
			}
			drawText(rightText, COLUMN_2, rightOptions);
		}
		addLineSpacing(1);
	}

	@Override
	public void drawSpeciesIconsRow(@Nullable Function<S, ItemStack> iconGetter) {
		drawText(Component.translatable("for.gui.active"), COLUMN_1);

		if (iconGetter != null) {
			drawIcon(COLUMN_1 + 43, -2, this.individual.getSpecies().cast(), iconGetter);
		}

		if (!this.haploid) {
			drawText(Component.translatable("for.gui.inactive"), COLUMN_2);

			if (iconGetter != null) {
				drawIcon(COLUMN_2 + 43, -2, this.individual.getInactiveSpecies().cast(), iconGetter);
			}
		}

		addLineSpacing(2);
	}

	private void drawIcon(int x, int y, S species, Function<S, ItemStack> iconGetter) {
		ItemStack stack = iconGetter.apply(species);
		if (stack == null || stack.isEmpty()) return;
		drawItemStack(x, y, stack);
	}

	private void drawItemStack(int x, int y, ItemStack stack) {
		GuiUtil.drawItemStack(this.graphics, this.font, stack, this.currentX + x, this.currentY + y);
	}

	@Override
	public void drawFertilityRow(IIntegerChromosome chromosome, ResourceLocation offspringSprite) {
		TextureAtlasSprite sprite = IForestryClientApi.INSTANCE.getTextureManager().getSprite(offspringSprite);

		drawChromosomeRow(chromosome, (active, c, a, options, text) -> {
			if (a.value() == 0) {
				return Component.translatable("allele.forestry.fertility.0i");
			} else {
				Component newText = text.copy().append(" x ");

				int x = this.currentX + (active ? COLUMN_1 : COLUMN_2) + this.font.width(newText) - 3;
				int y = this.currentY - 5;

				this.graphics.blit(x, y, 0, 16, 16, sprite);

				return newText;
			}
		});
	}

	@Override
	public void drawClimatePreferences(IValueChromosome<ToleranceType> temperatureTolerance, IValueChromosome<ToleranceType> humidityTolerance) {
		if (this.individual.getSpecies() instanceof IClimateSensitive active && this.individual.getInactiveSpecies() instanceof IClimateSensitive inactive) {
			drawClimatePreferencesSection(temperatureTolerance, species -> ClimateHelper.toDisplay(species.getTemperature()), active, inactive);
			drawClimatePreferencesSection(humidityTolerance, species -> ClimateHelper.toDisplay(species.getHumidity()), active, inactive);
		}
	}

	private void drawClimatePreferencesSection(IValueChromosome<ToleranceType> tolerance, Function<IClimateSensitive, Component> preference, IClimateSensitive active, IClimateSensitive inactive) {
		// draw first line with preferences
		{
			Component activeText = preference.apply(active);
			Component inactiveText = preference.apply(inactive);

			drawSplitLine(tolerance.getChromosomeDisplayName(), activeText, inactiveText, !this.haploid, (right, options, text) -> {
				options.setColor(ForestryColors.RECESSIVE_BLUE);
				return text;
			});
		}

		// draw second line with tolerances
		{
			this.currentX += 8;
			IValueAllele<ToleranceType> activeTolerance = this.genome.getActiveAllele(tolerance);
			IValueAllele<ToleranceType> inactiveTolerance = this.genome.getInactiveAllele(tolerance);
			Component activeText = tolerance.getDisplayName(activeTolerance);
			Component inactiveText = tolerance.getDisplayName(inactiveTolerance);

			drawSplitLine(Component.translatable("for.gui.tolerance"), activeText, inactiveText, !this.haploid, (right, options, text) -> {
				if (!right) {
					this.currentX += 6;
				}

				// draw icon
				IValueAllele<ToleranceType> allele = (right ? inactiveTolerance : activeTolerance);
				TextureAtlasSprite sprite = IForestryClientApi.INSTANCE.getTextureManager().getSprite(switch (allele.value()) {
					case BOTH_1, BOTH_2, BOTH_3, BOTH_4, BOTH_5 -> ForestrySprites.ANALYZER_TOLERANCE_BOTH;
					case DOWN_1, DOWN_2, DOWN_3, DOWN_4, DOWN_5 -> ForestrySprites.ANALYZER_TOLERANCE_DOWN;
					case UP_1, UP_2, UP_3, UP_4, UP_5 -> ForestrySprites.ANALYZER_TOLERANCE_UP;
					default -> ForestrySprites.ANALYZER_TOLERANCE_NONE;
				});
				int x = this.currentX - 16 + (right ? COLUMN_2 : COLUMN_1);
				int y = this.currentY - 4;
				this.graphics.blit(x, y, 0, 16, 16, sprite);

				// format text
				options.setColor(PortableAnalyzerScreen.getColorCoding(allele.dominant()));
				return Component.literal("(").append(text).append(")");
			});

			this.currentX -= 14;
		}

		addVerticalSpacing(2);
	}

	@Override
	public void drawProductList(Function<S, List<IProduct>> getProducts) {
		S active = (S) this.individual.getSpecies();
		S inactive = (S) this.individual.getInactiveSpecies();

		// collect list of products
		ArrayList<ItemStack> stacks;
		if (active == inactive || this.haploid) {
			List<IProduct> products = getProducts.apply(active);
			stacks = new ArrayList<>(products.size());

			for (var product : products) {
				stacks.add(product.createStack());
			}
		} else {
			List<IProduct> activeProducts = getProducts.apply(active);
			List<IProduct> inactiveProducts = getProducts.apply(inactive);
			ObjectOpenCustomHashSet<IProduct> products = new ObjectOpenCustomHashSet<>(activeProducts.size(), Product.ITEM_ONLY_STRATEGY);
			stacks = new ArrayList<>(products.size() + inactiveProducts.size());

			for (var product : activeProducts) {
				products.add(product);
				stacks.add(product.createStack());
			}
			for (var product : inactiveProducts) {
				if (!products.contains(product)) {
					stacks.add(product.createStack());
				}
			}
		}

		// draw the list
		int x = 0;
		int y = 0;
		for (ItemStack stack : stacks) {
			drawItemStack(x, y, stack);

			x += 18;
			if (x > 208) {
				x = 12;
				y += 18;
			}
		}
	}

	@Override
	public void drawText(FormattedCharSequence text, int x, @Nullable InteractableTextOptions options) {
		int color = 0xffffff;
		boolean dropShadow = false;

		int minX = this.currentX + x;
		int minY = this.currentY;

		if (options != null) {
			InteractableTextOptions.OnHover hover = options.onHover();
			if (hover != null) {
				int maxX = minX + this.font.width(text) - 1;

				if (minX <= this.mouseX && this.mouseX < maxX) {
					int maxY = minY + this.font.lineHeight;

					if (minY <= this.mouseY && this.mouseY < maxY) {
						hover.onHover(this.mouseX, this.mouseY);
					}
				}
			}

			color = options.color();
			dropShadow = options.dropShadow();
			text = withStyle(text, options.applyStyle(Style.EMPTY));
		}

		this.graphics.drawString(this.font, text, minX, minY, color, dropShadow);
	}

	@Override
	public void drawTextWrapped(Component text, @Nullable InteractableTextOptions options) {
		// todo account for currentX
		for (FormattedCharSequence line : this.font.split(text, 200)) {
			drawText(line, 0, options);
			addVerticalSpacing(9);
		}
	}

	@Override
	public void drawTooltip(int x, int y, List<Component> tooltip, List<@Nullable TextOptions> options) {
		int lineCount = tooltip.size();
		ArrayList<Component> styledTooltip = new ArrayList<>(lineCount);

		for (int i = 0; i < lineCount; i++) {
			if (i >= options.size()) {
				styledTooltip.add(tooltip.get(i));
			} else {
				TextOptions lineOptions = options.get(i);
				styledTooltip.add(lineOptions == null ? tooltip.get(i) : tooltip.get(i).plainCopy().withStyle(lineOptions::applyStyle));
			}
		}

		this.graphics.renderTooltip(this.font, styledTooltip, Optional.empty(), x, y);
	}

	@Override
	public int center(Component text) {
		return 100 - this.font.width(text) / 2;
	}

	@Override
	public void addHorizontalSpacing(int x) {
		this.currentX += x;
	}

	@Override
	public void addVerticalSpacing(int y) {
		this.currentY += y;
	}

	@Override
	public Font font() {
		return this.font;
	}

	@Override
	public void addLineSpacing(int lines, boolean compact) {
		this.currentY += (compact ? 10 : 12) * lines;
	}

	@Override
	public void setHaploid(boolean haploid) {
		this.haploid = haploid;
	}

	@Override
	public void drawMutationsPage(Function<S, ItemStack> iconGetter) {
		drawText(Component.translatable("for.gui.beealyzer.mutations").append(":"));
		addLineSpacing(1);

		ISpeciesType<?, ?> type = this.individual.getType();
		ISpecies<?> species = this.individual.getSpecies();

		Player player = Minecraft.getInstance().player;
		IBreedingTracker tracker = type.getBreedingTracker(player.level(), player.getGameProfile());

		int col = 0;
		IMutationManager<?> mutations = type.getMutations();
		for (IMutation<?> mutation : mutations.getMutationsFrom(species.cast())) {
			if (mutation.isSecret()) {
				continue;
			}

			drawMutation(mutation, tracker, iconGetter);
			addHorizontalSpacing(50);

			if (++col >= 4) {
				col = 0;
				addHorizontalSpacing(-200);
				addVerticalSpacing(16);
			}
		}
	}

	private void drawMutation(IMutation<?> mutation, IBreedingTracker tracker, Function<S, ItemStack> iconGetter) {
		boolean discovered = tracker.isDiscovered(mutation);
		boolean researched = tracker.isResearched(mutation);

		if (discovered) {
			// draw species icons
			S species = this.individual.getSpecies().cast();
			drawIcon(0, 0, mutation.getPartner(species).cast(), iconGetter);
			drawIcon(34, 0, mutation.getResult().cast(), iconGetter);
		} else {
			// draw question marks
			this.graphics.blit(this.texture, this.currentX, this.currentY, 78, 240, 16, 16);
			this.graphics.blit(this.texture, this.currentX + 33, this.currentY, 78, 240, 16, 16);
		}

		// draw arrow
		int textureU = 100 + 15 * switch (EnumMutateChance.rateChance(mutation.getChance())) {
			case HIGHER -> 1;
			case HIGH -> 2;
			case NORMAL -> 3;
			case LOW -> 4;
			case LOWEST -> 5;
			default -> 0;
		};
		this.graphics.blit(this.texture, this.currentX + 18, this.currentY + 4, textureU, 247, 15, 9);
		if (researched) {
			addVerticalSpacing(5);
			drawText(Component.literal("+"), 27, new InteractableTextOptions().setColor(ForestryColors.BLACK));
			addVerticalSpacing(-5);
		}
	}

	@Override
	public void drawTaxonomyPage() {
		// todo fold the : into the translation
		drawText(Component.translatable("for.gui.alyzer.classification").append(":"));
		addLineSpacing(1);

		ArrayDeque<ITaxon> hierarchy = new ArrayDeque<>();
		ISpecies<?> species = this.individual.getSpecies();
		ITaxon taxon = species.getGenus();
		while (taxon != null) {
			if (!taxon.name().isEmpty()) {
				hierarchy.push(taxon);
			}
			taxon = taxon.parent();
		}

		boolean overcrowded = hierarchy.size() > 5;
		int x = 0;
		ITaxon group;

		while (!hierarchy.isEmpty()) {
			group = hierarchy.pop();
			if (overcrowded && group.rank().isDroppable()) {
				continue;
			}

			String name = Character.toUpperCase(group.name().charAt(0)) + group.name().substring(1);
			drawText(Component.literal(name), x, new InteractableTextOptions().setColor(group.rank().getColour()));
			drawText(Component.literal(group.rank().name()), 158, new InteractableTextOptions().setColor(group.rank().getColour()));
			addLineSpacing(1, true);

			x += 12;
		}

		String binomial = species.getBinomial();
		if (this.font.width(binomial) > 96) {
			binomial = Character.toUpperCase(species.getGenusName().charAt(0)) + ". " + species.getSpeciesName();
		}

		drawText(Component.literal(binomial), x, new InteractableTextOptions().setColor(ForestryColors.GREEN));
		drawText(Component.literal("SPECIES"), 158, new InteractableTextOptions().setColor(ForestryColors.GREEN));
		addLineSpacing(1);

		drawText(Component.translatable("for.gui.alyzer.authority").append(": ").append(species.getAuthority()));
		addLineSpacing(1);

		String description = species.getDescriptionTranslationKey();
		if (Translator.canTranslateToLocal(description)) {
			String[] tokens = Component.translatable(description).getString().split("\\|");
			// draw the description split lines
			drawTextWrapped(Component.literal(tokens[0]), new InteractableTextOptions().setColor(ForestryColors.GRAY));

			if (tokens.length > 1) {
				addVerticalSpacing(1);
				String signature = "- " + tokens[1];
				// todo left aligned method
				drawText(Component.literal(signature), 210 - 12 - this.font.width(signature), new InteractableTextOptions().setColor(ForestryColors.YELLOW_GREEN).setDropShadow(true));
			}
		} else {
			// todo anchor to bottom of view
			drawTextWrapped(Component.translatable("for.gui.alyzer.nodescription"), new InteractableTextOptions().setColor(ForestryColors.GRAY));
		}
	}
}
