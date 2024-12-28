package forestry.api.genetics.alleles;

import forestry.api.apiculture.IFlowerType;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;
import forestry.api.lepidopterology.genetics.IButterflySpecies;

import static forestry.api.ForestryConstants.forestry;

public class ButterflyChromosomes {
	/**
	 * Determines the species of a butterfly.
	 */
	public static final IRegistryChromosome<IButterflySpecies> SPECIES = ForestryAlleles.REGISTRY.registryChromosome(ForestrySpeciesTypes.BUTTERFLY, IButterflySpecies.class);
	/**
	 * Determines physical size of a butterfly.
	 */
	public static final IFloatChromosome SIZE = ForestryAlleles.REGISTRY.floatChromosome(forestry("size"));
	/**
	 * Determines the flight speed of a butterfly.
	 */
	public static final IFloatChromosome SPEED = BeeChromosomes.SPEED;
	/**
	 * Determines how long this butterfly will live (todo change? mobs dying of old age isn't minecraft-y)
	 */
	public static final IIntegerChromosome LIFESPAN = ForestryAlleles.REGISTRY.intChromosome(forestry("butterfly_lifespan"));
	/**
	 * Determines the rate at which caterpillars destroy leaves and influences cocoon drops.
	 */
	public static final IIntegerChromosome METABOLISM = ForestryAlleles.REGISTRY.intChromosome(forestry("metabolism"));
	/**
	 * Determines how likely this butterfly is to mate with another butterfly as well as how fast its nurseries and cocoons mature.
	 */
	public static final IIntegerChromosome FERTILITY = BeeChromosomes.FERTILITY;
	/**
	 * Determines the acceptable range of temperatures from a butterfly's ideal temperature.
	 */
	public static final IValueChromosome<ToleranceType> TEMPERATURE_TOLERANCE = BeeChromosomes.TEMPERATURE_TOLERANCE;
	/**
	 * Determines the acceptable range of humidities from a butterfly's ideal humidity.
	 */
	public static final IValueChromosome<ToleranceType> HUMIDITY_TOLERANCE = BeeChromosomes.HUMIDITY_TOLERANCE;
	/**
	 * Whether diurnal butterflies can work during the night, or nocturnal butterflies (moths) can work during the day.
	 */
	public static final IBooleanChromosome NEVER_SLEEPS = ForestryAlleles.REGISTRY.booleanChromosome(forestry("never_sleeps"));
	/**
	 * Whether this butterfly can spawn or fly while it is raining.
	 */
	public static final IBooleanChromosome TOLERATES_RAIN = BeeChromosomes.TOLERATES_RAIN;
	/**
	 * Whether this butterfly is immune to fire/lava damage.
	 */
	public static final IBooleanChromosome FIREPROOF = ForestryAlleles.REGISTRY.booleanChromosome(forestry("fireproof"));
	/**
	 * Unimplemented.
	 */
	public static final IRegistryChromosome<IFlowerType> FLOWER_TYPE = BeeChromosomes.FLOWER_TYPE;
	/**
	 * Unimplemented.
	 */
	public static final IRegistryChromosome<IButterflyEffect> EFFECT = ForestryAlleles.REGISTRY.registryChromosome(forestry("butterfly_effect"), IButterflyEffect.class);
	/**
	 * Used for silk moths (Bombyx Mori) to affect cocoon drops.
	 */
	public static final IRegistryChromosome<IButterflyCocoon> COCOON = ForestryAlleles.REGISTRY.registryChromosome(forestry("cocoon"), IButterflyCocoon.class);
}
