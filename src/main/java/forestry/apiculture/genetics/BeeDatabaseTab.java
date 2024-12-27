package forestry.apiculture.genetics;

import java.util.function.Function;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.gatgets.DatabaseMode;
import forestry.api.genetics.gatgets.IDatabaseTab;
import forestry.core.gui.elements.Alignment;
import forestry.core.gui.elements.DatabaseElement;
import forestry.core.gui.elements.GuiElementFactory;
import forestry.core.utils.SpeciesUtil;

public class BeeDatabaseTab implements IDatabaseTab<IBee> {
	private final DatabaseMode mode;

	BeeDatabaseTab(DatabaseMode mode) {
		this.mode = mode;
	}

	@Override
	public DatabaseMode getMode() {
		return mode;
	}

	@Override
	public void createElements(DatabaseElement container, IBee bee, ILifeStage stage, ItemStack stack) {
		IBeeSpecies activeSpecies = bee.getGenome().getActiveValue(BeeChromosomes.SPECIES);
		IBeeSpecies inactiveSpecies = bee.getGenome().getInactiveValue(BeeChromosomes.SPECIES);

		container.label(Component.translatable("for.gui.database.tab." + (mode == DatabaseMode.ACTIVE ? "active" : "inactive") + "_species"), Alignment.TOP_CENTER, GuiElementFactory.INSTANCE.databaseTitle);

		container.addLine(Component.translatable("for.gui.species").withStyle(ChatFormatting.WHITE), BeeChromosomes.SPECIES);

		Function<Boolean, Component> toleranceText = a -> {
			IBeeSpecies species = a ? activeSpecies : inactiveSpecies;
			return ClimateHelper.toDisplay(species.getTemperature());
		};
		container.addLine(Component.translatable("for.gui.climate").withStyle(GuiElementFactory.INSTANCE.guiStyle), toleranceText, BeeChromosomes.TEMPERATURE_TOLERANCE);
		container.addToleranceLine(BeeChromosomes.TEMPERATURE_TOLERANCE);

		container.addLine(Component.translatable("for.gui.humidity"), toleranceText, BeeChromosomes.HUMIDITY_TOLERANCE);
		container.addToleranceLine(BeeChromosomes.HUMIDITY_TOLERANCE);

		container.addLine(Component.translatable("for.gui.lifespan"), BeeChromosomes.LIFESPAN);

		container.addLine(Component.translatable("for.gui.speed"), BeeChromosomes.SPEED);
		container.addLine(Component.translatable("for.gui.pollination"), BeeChromosomes.POLLINATION);
		container.addLine(Component.translatable("for.gui.flowers"), BeeChromosomes.FLOWER_TYPE);

		container.addFertilityLine(Component.translatable("for.gui.fertility"), BeeChromosomes.FERTILITY, 0);

		container.addLine(Component.translatable("for.gui.area"), BeeChromosomes.TERRITORY);
		container.addLine(Component.translatable("for.gui.effect"), BeeChromosomes.EFFECT);
		container.addLine(Component.translatable("for.gui.activity"), BeeChromosomes.ACTIVITY);

		Component yes = Component.translatable("for.yes");
		Component no = Component.translatable("for.no");

		Function<Boolean, Component> flyer = active -> {
			boolean toleratesRain = active ? bee.getGenome().getActiveValue(BeeChromosomes.TOLERATES_RAIN) : bee.getGenome().getInactiveValue(BeeChromosomes.TOLERATES_RAIN);
			return toleratesRain ? yes : no;
		};
		container.addLine(Component.translatable("for.gui.flyer"), flyer, BeeChromosomes.TOLERATES_RAIN);

		Function<Boolean, Component> cave = active -> {
			boolean caveDwelling = active ? bee.getGenome().getActiveValue(BeeChromosomes.CAVE_DWELLING) : bee.getGenome().getInactiveValue(BeeChromosomes.CAVE_DWELLING);
			return caveDwelling ? yes : no;
		};
		container.addLine(Component.translatable("for.gui.cave"), cave, BeeChromosomes.CAVE_DWELLING);

		if (stage == BeeLifeStage.PRINCESS || stage == BeeLifeStage.QUEEN) {
			Component displayTextKey = Component.translatable("for.bees.stock.pristine");
			if (!bee.isPristine()) {
				displayTextKey = Component.translatable("for.bees.stock.ignoble");
			}
			container.label(displayTextKey, Alignment.TOP_CENTER, GuiElementFactory.INSTANCE.binomial);
		}
	}

	@Override
	public ItemStack getIconStack() {
		return SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.MEADOWS, mode == DatabaseMode.ACTIVE ? BeeLifeStage.PRINCESS : BeeLifeStage.DRONE);
	}
}
