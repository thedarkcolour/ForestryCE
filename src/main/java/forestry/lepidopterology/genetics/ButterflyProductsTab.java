package forestry.lepidopterology.genetics;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;

import forestry.api.core.IProduct;
import forestry.api.genetics.ILifeStage;
import forestry.api.core.Product;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.gatgets.IDatabaseTab;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.core.gui.elements.Alignment;
import forestry.core.gui.elements.DatabaseElement;
import forestry.core.gui.elements.GuiElementFactory;
import forestry.core.gui.elements.ItemElement;
import forestry.core.gui.elements.layouts.FlexLayout;
import forestry.core.gui.elements.layouts.LayoutHelper;
import forestry.core.utils.SpeciesUtil;

public class ButterflyProductsTab implements IDatabaseTab<IButterfly> {
	@Override
	public void createElements(DatabaseElement container, IButterfly individual, ILifeStage stage, ItemStack itemStack) {
		LayoutHelper groupHelper = container.layoutHelper((x, y) -> GuiElementFactory.horizontal(18, 2, FlexLayout.LEFT_MARGIN), 90, 0);
		Collection<IProduct> butterflyLoot = individual.getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getButterflyLoot();
		if (!butterflyLoot.isEmpty()) {
			container.translated("for.gui.loot.butterfly").setAlign(Alignment.TOP_CENTER);
			butterflyLoot.forEach(stack -> groupHelper.add(new ItemElement(0, 0, stack.createStack())));
			groupHelper.finish();
		}

		Collection<IProduct> caterpillarLoot = individual.getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getCaterpillarProducts();
		if (!caterpillarLoot.isEmpty()) {
			container.translated("for.gui.loot.caterpillar").setAlign(Alignment.TOP_CENTER);
			caterpillarLoot.forEach(stack -> groupHelper.add(new ItemElement(0, 0, stack.createStack())));
			groupHelper.finish();
		}

		Collection<IProduct> cocoonLoot = individual.getGenome().getActiveValue(ButterflyChromosomes.COCOON).getProducts();
		if (!cocoonLoot.isEmpty()) {
			container.translated("for.gui.loot.cocoon").setAlign(Alignment.TOP_CENTER);
			cocoonLoot.forEach(stack -> groupHelper.add(new ItemElement(0, 0, stack.createStack())));
			groupHelper.finish();
		}
	}


	@Override
	public ItemStack getIconStack() {
		return SpeciesUtil.BUTTERFLY_TYPE.get().createStack(ForestryButterflySpecies.AURORA, ButterflyLifeStage.SERUM);
	}
}
