package forestry.lepidopterology.genetics;

import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.gatgets.DatabaseMode;
import forestry.api.genetics.gatgets.IDatabaseTab;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.core.gui.elements.Alignment;
import forestry.core.gui.elements.DatabaseElement;
import forestry.core.gui.elements.GuiElementFactory;
import forestry.core.utils.SpeciesUtil;

public class ButterflyDatabaseTab implements IDatabaseTab<IButterfly> {
	private final DatabaseMode mode;

	ButterflyDatabaseTab(DatabaseMode mode) {
		this.mode = mode;
	}

	@Override
	public DatabaseMode getMode() {
		return mode;
	}

	@Override
	public void createElements(DatabaseElement database, IButterfly butterfly, ILifeStage stage, ItemStack stack) {
		IButterflySpecies activeSpecies = butterfly.getGenome().getActiveValue(ButterflyChromosomes.SPECIES);
		IButterflySpecies inactiveSpecies = butterfly.getGenome().getInactiveValue(ButterflyChromosomes.SPECIES);

		database.label(Component.translatable("for.gui.database.tab." + (mode == DatabaseMode.ACTIVE ? "active" : "inactive") + "_species.name"), Alignment.TOP_CENTER, GuiElementFactory.INSTANCE.databaseTitle);

		database.addLine(Component.translatable("for.gui.species"), ButterflyChromosomes.SPECIES);

		database.addLine(Component.translatable("for.gui.size"), ButterflyChromosomes.SIZE);

		database.addLine(Component.translatable("for.gui.lifespan"), ButterflyChromosomes.LIFESPAN);

		database.addLine(Component.translatable("for.gui.speed"), ButterflyChromosomes.SPEED);

		database.addLine(Component.translatable("for.gui.metabolism"), ButterflyChromosomes.METABOLISM);

		database.addFertilityLine(Component.translatable("for.gui.fertility"), ButterflyChromosomes.FERTILITY, 8);

		database.addLine(Component.translatable("for.gui.flowers"), ButterflyChromosomes.FLOWER_TYPE);
		database.addLine(Component.translatable("for.gui.effect"), ButterflyChromosomes.EFFECT);

		Function<Boolean, Component> toleranceText = a -> {
			IButterflySpecies species = a ? activeSpecies : inactiveSpecies;
			return ClimateHelper.toDisplay(species.getTemperature());
		};
		database.addLine(Component.translatable("for.gui.climate"), toleranceText, ButterflyChromosomes.TEMPERATURE_TOLERANCE);
		database.addToleranceLine(ButterflyChromosomes.TEMPERATURE_TOLERANCE);

		database.addLine(Component.translatable("for.gui.humidity"), toleranceText, ButterflyChromosomes.HUMIDITY_TOLERANCE);
		database.addToleranceLine(ButterflyChromosomes.HUMIDITY_TOLERANCE);

		Component yes = Component.translatable("for.yes");
		Component no = Component.translatable("for.no");

		{
			Component diurnalFirst;
			Component diurnalSecond;
			Component nocturnalFirst;
			Component nocturnalSecond;
			if (butterfly.getGenome().getActiveValue(ButterflyChromosomes.NEVER_SLEEPS)) {
				nocturnalFirst = diurnalFirst = yes;
			} else {
				nocturnalFirst = activeSpecies.isNocturnal() ? yes : no;
				diurnalFirst = !activeSpecies.isNocturnal() ? yes : no;
			}
			if (butterfly.getGenome().getInactiveValue(ButterflyChromosomes.NEVER_SLEEPS)) {
				nocturnalSecond = diurnalSecond = yes;
			} else {
				nocturnalSecond = inactiveSpecies.isNocturnal() ? yes : no;
				diurnalSecond = !inactiveSpecies.isNocturnal() ? yes : no;
			}

			database.addLine(Component.translatable("for.gui.diurnal"), (Boolean a) -> a ? diurnalFirst : diurnalSecond, false);
			database.addLine(Component.translatable("for.gui.nocturnal"), (Boolean a) -> a ? nocturnalFirst : nocturnalSecond, false);
		}

		Function<Boolean, Component> flyer = active -> {
			boolean tolerantFlyer = active ? butterfly.getGenome().getActiveValue(ButterflyChromosomes.TOLERATES_RAIN) : butterfly.getGenome().getInactiveValue(ButterflyChromosomes.TOLERATES_RAIN);
			return tolerantFlyer ? yes : no;
		};
		database.addLine(ButterflyChromosomes.TOLERATES_RAIN.getChromosomeDisplayName(), flyer, ButterflyChromosomes.TOLERATES_RAIN);

		Function<Boolean, Component> fireResist = active -> {
			boolean fireResistant = active ? butterfly.getGenome().getActiveValue(ButterflyChromosomes.FIREPROOF) : butterfly.getGenome().getInactiveValue(ButterflyChromosomes.FIREPROOF);
			return fireResistant ? yes : no;
		};
		database.addLine(ButterflyChromosomes.FIREPROOF.getChromosomeDisplayName(), fireResist, ButterflyChromosomes.FIREPROOF);
	}

	@Override
	public ItemStack getIconStack() {
		return SpeciesUtil.BUTTERFLY_TYPE.get().createStack(ForestryButterflySpecies.BLUE_WING, mode == DatabaseMode.ACTIVE ? ButterflyLifeStage.BUTTERFLY : ButterflyLifeStage.CATERPILLAR);
	}
}
