package forestry.apiculture.client.plugin;

import com.google.common.collect.Lists;
import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.client.ForestrySprites;
import forestry.api.client.InteractableTextOptions;
import forestry.api.client.TextOptions;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.core.genetics.ILifeStage;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.core.engine.genetics.flowers.TagFlowerType;
import forestry.core.platform.client.ForestryColors;
import forestry.core.platform.util.TranslationKeys;
import forestry.core.platform.config.ForestryConfig;
import forestry.core.platform.gui.ForestryScreen;
import forestry.core.content.analyzer.PortableAnalyzerScreen;
import forestry.core.platform.util.GeneticsUtil;
import forestry.core.platform.util.SpeciesUtil;
import forestry.core.platform.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeeAnalyzerPlugin implements IAnalyzerPlugin<IBeeSpecies, IBee> {
	private final Map<ISpecies<?>, ItemStack> iconStacks = GeneticsUtil.getIconStacks(BeeLifeStage.DRONE, SpeciesUtil.BEE_TYPE.get());

	private static <V> Component addHoverDescription(IAnalyzerGraphics<?, ?> graphics, IChromosome<V> chromosome, V value, InteractableTextOptions options, Component text) {
		options.setOnHover((x, y) -> {
			String effectDescription = chromosome.translationKey(value) + ".desc";

			if (Translator.canTranslateToLocal(effectDescription)) {
				graphics.drawTooltip(x, y, Component.translatable(effectDescription));
				options.setUnderlined(true);
			}
		});

		return text;
	}

	private static <V> Component addFlowerTypeTooltip(IAnalyzerGraphics<?, ?> graphics, IChromosome<V> chromosome, V value, InteractableTextOptions options, Component text) {
		if (value instanceof ResourceLocation id && IForestryApi.INSTANCE.getFlowerTypeManager().getFlowerTypeSafe(id) instanceof TagFlowerType type) {
			options.setOnHover((x, y) -> {
				ArrayList<Component> lines = Lists.newArrayList(Component.literal("Accepts the following:"), Component.literal("#" + type.acceptableFlowers().location()));
				ArrayList<TextOptions> lineOptions = Lists.newArrayList(null, new TextOptions().setColor(ForestryColors.LIGHT_GRAY));
				TextOptions gray = new TextOptions().setColor(ForestryColors.GRAY);

				if (Screen.hasShiftDown()) {
					ClientLevel level = Minecraft.getInstance().level;
					level.registryAccess().registryOrThrow(Registries.BLOCK).getTag(type.acceptableFlowers()).ifPresent(list -> {
						int length = list.size();
						int entries = Math.min(5, length);
						boolean cycle = length > 5;
						int offset = cycle ? (int) (level.getGameTime() / 40L) % length : 0;

						for (int i = 0; i < entries; i++) {
							lines.add(list.get((offset + i) % length).value().getName());
							lineOptions.add(gray);
						}
						if (cycle) {
							lines.add(Component.literal("..."));
							lineOptions.add(gray);
						}
					});
				} else {
					lines.add(Component.translatable(TranslationKeys.HOLD_SHIFT_FOR_DETAILS));
					lineOptions.add(gray);
				}

				graphics.drawTooltip(x, y, lines, lineOptions);
				options.setUnderlined(true);
			});
		} else {
			addHoverDescription(graphics, chromosome, value, options, text);
		}

		return text;
	}

	@Override
	public void drawPage1(IAnalyzerGraphics<IBeeSpecies, IBee> graphics, IBee individual, ILifeStage stage, ItemStack specimen) {
		graphics.setHaploid(ForestryConfig.SERVER.useHaploidDrones.get() && stage == BeeLifeStage.DRONE);
		graphics.drawSpeciesIconsRow(this.iconStacks::get);
		graphics.drawChromosomeRow(BeeChromosomes.SPECIES);
		graphics.addLineSpacing(1);
		graphics.drawChromosomeRow(BeeChromosomes.LIFESPAN);
		graphics.drawChromosomeRow(BeeChromosomes.SPEED);
		graphics.drawChromosomeRow(BeeChromosomes.POLLINATION);
		graphics.drawChromosomeRow(BeeChromosomes.FLOWER_TYPE, (active, chromosome, allele, options, text) -> addFlowerTypeTooltip(graphics, chromosome, allele, options, text));
		graphics.drawFertilityRow(BeeChromosomes.FERTILITY, ForestrySprites.ANALYZER_BEE_FERTILITY);
		graphics.drawChromosomeRow(BeeChromosomes.TERRITORY);
		graphics.drawChromosomeRow(BeeChromosomes.EFFECT, (active, chromosome, allele, options, text) -> addHoverDescription(graphics, chromosome, allele, options, text));
	}

	@Override
	public void drawPage2(IAnalyzerGraphics<IBeeSpecies, IBee> graphics, IBee individual, ILifeStage stage, ItemStack specimen) {
		graphics.setHaploid(ForestryConfig.SERVER.useHaploidDrones.get() && stage == BeeLifeStage.DRONE);
		graphics.drawSpeciesIconsRow(null);
		graphics.drawClimatePreferences(BeeChromosomes.TEMPERATURE_TOLERANCE, BeeChromosomes.HUMIDITY_TOLERANCE);
		graphics.drawChromosomeRow(BeeChromosomes.ACTIVITY);
		graphics.drawChromosomeRow(BeeChromosomes.TOLERATES_RAIN, (active, c, a, options, text) -> {
			options.setColor(PortableAnalyzerScreen.getColorCoding(false));
			return text;
		});
		graphics.drawChromosomeRow(BeeChromosomes.CAVE_DWELLING, (active, c, a, options, text) -> {
			options.setColor(PortableAnalyzerScreen.getColorCoding(false));
			return text;
		});

		if (stage == BeeLifeStage.PRINCESS || stage == BeeLifeStage.QUEEN) {
			boolean pristine = individual.isPristine();
			Component text = Component.translatable(pristine ? "for.bees.stock.pristine" : "for.bees.stock.ignoble")
				.withStyle(style -> style
					.withColor(0x14d50b)
					.withItalic(pristine));
			graphics.drawText(text, graphics.center(text), new InteractableTextOptions().setColor(0x14d50b).setItalic(pristine));
			graphics.addLineSpacing(1);
			if (individual.getGeneration() > 0) {
				Component generations = Component.translatable("for.gui.beealyzer.generations", individual.getGeneration());
				graphics.drawText(generations, 0, new InteractableTextOptions().setColor(0x14d50b));
				graphics.addLineSpacing(1);
			}
		}
	}

	@Override
	public void drawPage3(IAnalyzerGraphics<IBeeSpecies, IBee> graphics, IBee individual, ILifeStage stage, ItemStack specimen) {
		graphics.setHaploid(ForestryConfig.SERVER.useHaploidDrones.get() && stage == BeeLifeStage.DRONE);
		graphics.drawText(Component.translatable("for.gui.beealyzer.produce").append(Component.literal(":")));
		graphics.addLineSpacing(1);
		graphics.drawProductList(IProductProducer::getProducts);

		graphics.addLineSpacing(4);

		// set as haploid to exclude inactive specialties
		graphics.setHaploid(true);
		graphics.drawText(Component.translatable("for.gui.beealyzer.specialty").append(Component.literal(":")));
		graphics.addLineSpacing(1);
		graphics.drawProductList(ISpecialtyProducer::getSpecialties);

		// 1.7 Forestry used to print the jubilance condition here. should I add it back?
	}

	@Override
	public void drawPage4(IAnalyzerGraphics<IBeeSpecies, IBee> graphics, IBee individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawMutationsPage(this.iconStacks::get);
	}

	@Override
	public List<String> getHints() {
		return ForestryScreen.HINTS.get("beealyzer");
	}
}
