package forestry.apiculture.bees;

import com.google.common.base.Preconditions;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.HumidityType;
import forestry.api.core.IProduct;
import forestry.api.core.TemperatureType;
import forestry.api.core.genetics.ClimateHelper;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.alleles.ForestryAlleles;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.apiculture.bees.genetics.Bee;
import forestry.core.engine.genetics.Species;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.List;
import forestry.api.apiculture.ForestryActivityTypes;
import forestry.core.platform.util.GeneticsUtil;

public class BeeSpecies extends Species<IBeeSpeciesType, IBee> implements IBeeSpecies {
	private final List<IProduct> products;
	private final List<IProduct> specialties;
	private final TemperatureType temperature;
	private final HumidityType humidity;
	private final IBeeJubilance jubilance;
	private final int body;
	private final int outline;
	private final int stripes;

	public BeeSpecies(ResourceLocation id, IBeeSpeciesType speciesType, IGenome defaultGenome, IBeeSpeciesBuilder builder) {
		super(id, speciesType, defaultGenome, builder);

		this.products = builder.buildProducts();
		this.specialties = builder.buildSpecialties();
		this.temperature = builder.getTemperature();
		this.humidity = builder.getHumidity();
		this.jubilance = speciesType.getJubilance(builder.getJubilance());
		this.body = builder.getBody();
		this.outline = builder.getOutline();
		this.stripes = builder.getStripes();
	}

	@Override
	public List<IProduct> getProducts() {
		return this.products;
	}

	@Override
	public List<IProduct> getSpecialties() {
		return this.specialties;
	}

	@Override
	public TemperatureType getTemperature() {
		return this.temperature;
	}

	@Override
	public HumidityType getHumidity() {
		return this.humidity;
	}

	@Override
	public boolean isJubilant(IGenome genome, IBeeHousing housing) {
		return this.jubilance.isJubilant(this, genome, housing);
	}

	@Override
	public IBee createIndividual(IGenome genome) {
		Preconditions.checkArgument(genome.getKaryotype() == getKaryotype());

		return new Bee(genome);
	}

	@Override
	public int getBody() {
		return this.body;
	}

	@Override
	public int getStripes() {
		return this.stripes;
	}

	@Override
	public int getOutline() {
		return this.outline;
	}

	@Override
	public int getEscritoireColor() {
		return this.escritoireColor == -1 ? this.outline : this.escritoireColor;
	}

	@Override
	public void addTooltip(IBee individual, List<Component> tooltip) {
		// No info 4 u!
		if (!individual.isAnalyzed()) {
			addUnknownGenomeTooltip(tooltip);
			return;
		}

		IGenome genome = individual.getGenome();

		// You analyzed it? Juicy tooltip coming up!
		addHybridTooltip(tooltip, genome, BeeChromosomes.SPECIES, "for.bees.hybrid");

		int generation = individual.getGeneration();

		if (generation > 0) {
			Rarity rarity;
			if (generation >= 1000) {
				rarity = Rarity.EPIC;
			} else if (generation >= 100) {
				rarity = Rarity.RARE;
			} else if (generation >= 10) {
				rarity = Rarity.UNCOMMON;
			} else {
				rarity = Rarity.COMMON;
			}
			tooltip.add(Component.translatable("for.gui.beealyzer.generations", generation).withStyle(rarity.getStyleModifier()));
		}

		tooltip.add(GeneticsUtil.getActiveName(genome, BeeChromosomes.LIFESPAN).append(" ").append(Component.translatable("for.gui.life")).withStyle(ChatFormatting.GRAY));
		tooltip.add(GeneticsUtil.getActiveName(genome, BeeChromosomes.SPEED).append(" ").append(Component.translatable("for.gui.worker")).withStyle(ChatFormatting.GRAY));

		Component tempToleranceAllele = GeneticsUtil.getActiveName(genome, BeeChromosomes.TEMPERATURE_TOLERANCE);
		Component humidToleranceAllele = GeneticsUtil.getActiveName(genome, BeeChromosomes.HUMIDITY_TOLERANCE);
		IBeeSpecies active = genome.resolveActive(BeeChromosomes.SPECIES);

		tooltip.add(Component.literal("T: ").append(ClimateHelper.toDisplay(active.getTemperature())).append(" / ").append(tempToleranceAllele).withStyle(ChatFormatting.GREEN));
		tooltip.add(Component.literal("H: ").append(ClimateHelper.toDisplay(active.getHumidity())).append(" / ").append(humidToleranceAllele).withStyle(ChatFormatting.GREEN));

		tooltip.add(GeneticsUtil.getActiveName(genome, BeeChromosomes.FLOWER_TYPE).withStyle(ChatFormatting.GRAY));

		ResourceLocation activity = genome.getActiveValue(BeeChromosomes.ACTIVITY);
		if (!activity.equals(ForestryActivityTypes.DIURNAL)) {
			tooltip.add(GeneticsUtil.getActiveName(genome, BeeChromosomes.ACTIVITY).withStyle(ChatFormatting.GOLD));
		}

		if (genome.getActiveValue(BeeChromosomes.TOLERATES_RAIN)) {
			tooltip.add(Component.translatable("chromosome.forestry.tolerates_rain.tooltip").withStyle(ChatFormatting.WHITE));
		}
	}
}
