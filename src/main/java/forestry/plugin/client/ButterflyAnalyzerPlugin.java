package forestry.plugin.client;

import forestry.api.client.ForestrySprites;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;

public class ButterflyAnalyzerPlugin implements IAnalyzerPlugin<IButterflySpecies, IButterfly> {
	private final IdentityHashMap<ISpecies<?>, ItemStack> iconStacks = GeneticsUtil.getIconStacks(ButterflyLifeStage.BUTTERFLY, SpeciesUtil.BUTTERFLY_TYPE.get());

	@Override
	public void drawPage1(IAnalyzerGraphics<IButterflySpecies, IButterfly> graphics, IButterfly individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawSpeciesIconsRow(this.iconStacks::get);
		graphics.drawChromosomeRow(ButterflyChromosomes.SPECIES);
		graphics.addLineSpacing(1);
		graphics.drawChromosomeRow(ButterflyChromosomes.SIZE);
		graphics.drawChromosomeRow(ButterflyChromosomes.SPEED);
		graphics.drawChromosomeRow(ButterflyChromosomes.METABOLISM);
		graphics.drawFertilityRow(ButterflyChromosomes.FERTILITY, ForestrySprites.ANALYZER_BUTTERFLY_FERTILITY);
		graphics.drawChromosomeRow(ButterflyChromosomes.FLOWER_TYPE);
		graphics.drawChromosomeRow(ButterflyChromosomes.EFFECT);
	}

	@Override
	public void drawPage2(IAnalyzerGraphics<IButterflySpecies, IButterfly> graphics, IButterfly individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawSpeciesIconsRow(null);
		graphics.drawClimatePreferences(ButterflyChromosomes.TEMPERATURE_TOLERANCE, ButterflyChromosomes.HUMIDITY_TOLERANCE);
		graphics.drawChromosomeRow(ButterflyChromosomes.NEVER_SLEEPS);
		graphics.drawChromosomeRow(ButterflyChromosomes.TOLERATES_RAIN);
		graphics.drawChromosomeRow(ButterflyChromosomes.FIREPROOF);
	}

	@Override
	public void drawPage3(IAnalyzerGraphics<IButterflySpecies, IButterfly> graphics, IButterfly individual, ILifeStage stage, ItemStack specimen) {
		// Set haploid since original Butterfly alyzer plugin only displayed active allele
		graphics.setHaploid(true);

		graphics.drawText(Component.translatable("for.gui.loot.butterfly").append(":"));
		graphics.addLineSpacing(1);
		graphics.drawProductList(IButterflySpecies::getButterflyLoot);

		graphics.addLineSpacing(2);

		graphics.drawText(Component.translatable("for.gui.loot.caterpillar").append(":"));
		graphics.addLineSpacing(1);
		graphics.drawProductList(IButterflySpecies::getCaterpillarProducts);

		graphics.addLineSpacing(2);

		graphics.drawText(Component.translatable("for.gui.loot.cocoon").append(":"));
		graphics.addLineSpacing(1);
		// since we're haploid, we can just return a list and it will only be used once
		graphics.drawProductList(s -> individual.getGenome().getActiveValue(ButterflyChromosomes.COCOON).getProducts());
	}

	@Override
	public void drawPage4(IAnalyzerGraphics<IButterflySpecies, IButterfly> graphics, IButterfly individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawMutationsPage(this.iconStacks::get);
	}
}
