package forestry.plugin.client;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class TreeAnalyzerPlugin implements IAnalyzerPlugin<ITreeSpecies, ITree> {
	private final Map<ISpecies<?>, ItemStack> iconStacks = GeneticsUtil.getIconStacks(TreeLifeStage.SAPLING, SpeciesUtil.TREE_TYPE.get());

	@Override
	public void drawPage1(IAnalyzerGraphics<ITreeSpecies, ITree> graphics, ITree individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawSpeciesIconsRow(this.iconStacks::get);
		graphics.drawChromosomeRow(TreeChromosomes.SPECIES);
		graphics.addLineSpacing(1);

		graphics.drawChromosomeRow(TreeChromosomes.SAPLINGS);
		graphics.drawChromosomeRow(TreeChromosomes.MATURATION);
		graphics.drawChromosomeRow(TreeChromosomes.HEIGHT);
		graphics.drawChromosomeRow(TreeChromosomes.GIRTH);
		graphics.drawChromosomeRow(TreeChromosomes.YIELD);
		graphics.drawChromosomeRow(TreeChromosomes.SAPPINESS);
		graphics.drawChromosomeRow(TreeChromosomes.EFFECT);
	}

	@Override
	public void drawPage2(IAnalyzerGraphics<ITreeSpecies, ITree> graphics, ITree individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawSpeciesIconsRow(null);
		graphics.drawChromosomeRow(TreeChromosomes.FIREPROOF);
		graphics.drawChromosomeRow(TreeChromosomes.FRUIT);
	}

	@Override
	public void drawPage3(IAnalyzerGraphics<ITreeSpecies, ITree> graphics, ITree individual, ILifeStage stage, ItemStack specimen) {
		// trees only grow fruit from their active allele
		graphics.setHaploid(true);
		graphics.drawText(Component.translatable("for.gui.beealyzer.produce").append(Component.literal(":")));
		graphics.addLineSpacing(1);
		graphics.drawProductList(s -> individual.getProducts());

		graphics.addLineSpacing(4);

		graphics.drawText(Component.translatable("for.gui.beealyzer.specialty").append(Component.literal(":")));
		graphics.addLineSpacing(1);
		graphics.drawProductList(s -> individual.getSpecialties());
	}

	@Override
	public void drawPage4(IAnalyzerGraphics<ITreeSpecies, ITree> graphics, ITree individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawMutationsPage(this.iconStacks::get);
	}
}
