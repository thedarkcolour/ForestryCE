package forestry.arboriculture.genetics;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IValueAllele;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.genetics.gatgets.DatabaseMode;
import forestry.api.genetics.gatgets.IDatabaseTab;
import forestry.core.gui.GuiConstants;
import forestry.core.gui.elements.Alignment;
import forestry.core.gui.elements.DatabaseElement;
import forestry.core.gui.elements.GuiElementFactory;
import forestry.core.utils.SpeciesUtil;

@OnlyIn(Dist.CLIENT)
public class TreeDatabaseTab implements IDatabaseTab<ITree> {
	private final DatabaseMode mode;

	TreeDatabaseTab(DatabaseMode mode) {
		this.mode = mode;
	}

	@Override
	public DatabaseMode getMode() {
		return mode;
	}

	@Override
	public void createElements(DatabaseElement container, ITree tree, ILifeStage stage, ItemStack stack) {
		AllelePair<IValueAllele<ITreeSpecies>> speciesPair = tree.getGenome().getAllelePair(TreeChromosomes.SPECIES);
		ITreeSpecies species = (mode == DatabaseMode.ACTIVE ? speciesPair.active() : speciesPair.inactive()).value();
		Style speciesStyle = GuiElementFactory.INSTANCE.getStateStyle(species.isDominant());

		container.translated("for.gui.database.tab." + (mode == DatabaseMode.ACTIVE ? "active" : "inactive") + "_species.name").setStyle(GuiElementFactory.INSTANCE.databaseTitle).setAlign(Alignment.TOP_CENTER);

		container.addLine(Component.translatable("for.gui.species"), TreeChromosomes.SPECIES);

		container.addLine(Component.translatable("for.gui.saplings"), TreeChromosomes.SAPLINGS);
		container.addLine(Component.translatable("for.gui.maturity"), TreeChromosomes.MATURATION);
		container.addLine(Component.translatable("for.gui.height"), TreeChromosomes.HEIGHT);

		container.addLine(Component.translatable("for.gui.girth"), (girth, active) -> Component.literal(String.format("%sx%s", girth.value(), girth.value())), TreeChromosomes.GIRTH);

		container.addLine(Component.translatable("for.gui.yield"), TreeChromosomes.YIELD);
		container.addLine(Component.translatable("for.gui.sappiness"), TreeChromosomes.SAPPINESS);

		container.addLine(Component.translatable("for.gui.effect"), TreeChromosomes.EFFECT);

		//container.addLine(Component.translatable("for.gui.native"), Component.translatable("for.gui." + primarySpecies.getPlantType().toString().toLowerCase(Locale.ENGLISH)), species.isDominant());

		//container.label(Component.translatable("for.gui.supports"), Alignment.TOP_CENTER, GuiConstants.UNDERLINED_STYLE);
		//List<IFruitFamily> families = new ArrayList<>(primarySpecies.getSuitableFruit());

		//for (IFruitFamily fruitFamily : families) {
		//	container.label(fruitFamily.getName(), Alignment.TOP_CENTER, speciesStyle);
		//}

		IValueAllele<IFruit> fruit = mode == DatabaseMode.ACTIVE ? tree.getGenome().getActiveAllele(TreeChromosomes.FRUIT) : tree.getGenome().getInactiveAllele(TreeChromosomes.FRUIT);
		Style textStyle = GuiElementFactory.INSTANCE.getStateStyle(tree.getGenome().getActiveAllele(TreeChromosomes.FRUIT).dominant());

		container.translated("for.gui.fruits").setStyle(GuiConstants.UNDERLINED_STYLE).setAlign(Alignment.TOP_CENTER);

		//if (!species.getSuitableFruit().contains(fruit.getProvider().getFamily()) && fruit != AlleleFruits.fruitNone) {
		//	fruitStyle = fruitStyle.withStrikethrough(true);
		//}
		container.label(TreeChromosomes.FRUIT.getDisplayName(fruit)).setStyle(textStyle).setAlign(Alignment.TOP_CENTER);

		//IFruitFamily family = fruit.getProvider().getFamily();

		//if (!family.getUID().equals(EnumFruitFamily.NONE.getUID())) {
		//	container.label(Component.translatable("for.gui.family"), Alignment.TOP_CENTER, GuiConstants.UNDERLINED_STYLE);
		//	container.label(family.getName(), Alignment.TOP_CENTER, textStyle);
		//}
	}

	@Override
	public ItemStack getIconStack() {
		return SpeciesUtil.getTreeSpecies(ForestryTreeSpecies.BUSH_CHERRY).createStack(mode == DatabaseMode.ACTIVE ? TreeLifeStage.SAPLING : TreeLifeStage.POLLEN);
	}
}
