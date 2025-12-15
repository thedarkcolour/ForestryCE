package forestry.plugin.client;

import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.client.ForestrySprites;
import forestry.api.client.InteractableTextOptions;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.core.config.ForestryConfig;
import forestry.core.gui.PortableAnalyzerScreen;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;

public class BeeAnalyzerPlugin implements IAnalyzerPlugin<IBeeSpecies, IBee> {
	private final Map<ISpecies<?>, ItemStack> iconStacks = GeneticsUtil.getIconStacks(BeeLifeStage.DRONE, SpeciesUtil.BEE_TYPE.get());

	@Override
	public void drawPage1(IAnalyzerGraphics<IBeeSpecies, IBee> graphics, IBee individual, ILifeStage stage, ItemStack specimen) {
		graphics.setHaploid(ForestryConfig.SERVER.useHaploidDrones.get() && stage == BeeLifeStage.DRONE);
		graphics.drawSpeciesIconsRow(iconStacks::get);
		graphics.drawChromosomeRow(BeeChromosomes.SPECIES);
		graphics.addLineSpacing(1);
		graphics.drawChromosomeRow(BeeChromosomes.LIFESPAN);
		graphics.drawChromosomeRow(BeeChromosomes.SPEED);
		graphics.drawChromosomeRow(BeeChromosomes.POLLINATION);
		graphics.drawChromosomeRow(BeeChromosomes.FLOWER_TYPE);
		graphics.drawFertilityRow(BeeChromosomes.FERTILITY, ForestrySprites.ANALYZER_BEE_FERTILITY);
		graphics.drawChromosomeRow(BeeChromosomes.TERRITORY);
		graphics.drawChromosomeRow(BeeChromosomes.EFFECT, (active, c, a, options, text) -> {
			options.setOnHover((x, y) -> {
				graphics.drawTooltip(x, y, Component.literal("Testing"));
				options.setUnderlined(true);
			});
			return text;
		});
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
			// todo center text
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
		graphics.drawText(Component.translatable("for.gui.beealyzer.produce"));
		graphics.drawProductList(IProductProducer::getProducts);

		// set as haploid to exclude inactive specialties
		graphics.setHaploid(true);
		graphics.drawText(Component.translatable("for.gui.beealyzer.specialty"));
		graphics.drawProductList(ISpecialtyProducer::getSpecialties);
	}
}
