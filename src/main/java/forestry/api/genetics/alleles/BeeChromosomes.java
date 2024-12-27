package forestry.api.genetics.alleles;

import net.minecraft.core.Vec3i;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.ForestrySpeciesTypes;

import static forestry.api.ForestryConstants.forestry;

/**
 * All chromosomes of the Forestry bee species.
 */
public class BeeChromosomes {
	/**
	 * The species of a bee.
	 */
	public static final IRegistryChromosome<IBeeSpecies> SPECIES = ForestryAlleles.REGISTRY.registryChromosome(ForestrySpeciesTypes.BEE, IBeeSpecies.class);
	/**
	 * Determines a queen's production speed. Shows up as "worker" in the portable analyzer.
	 */
	public static final IFloatChromosome SPEED = ForestryAlleles.REGISTRY.floatChromosome(forestry("speed"));
	/**
	 * Determines a queen's lifespan.
	 */
	public static final IIntegerChromosome LIFESPAN = ForestryAlleles.REGISTRY.intChromosome(forestry("lifespan"));
	/**
	 * The number of drones given when a queen dies.
	 */
	public static final IIntegerChromosome FERTILITY = ForestryAlleles.REGISTRY.intChromosome(forestry("fertility"));
	/**
	 * Determines the acceptable range of temperatures from a bee's ideal temperature. Reused by butterflies.
	 */
	public static final IValueChromosome<ToleranceType> TEMPERATURE_TOLERANCE = ForestryAlleles.REGISTRY.valueChromosome(forestry("temperature_tolerance"), ToleranceType.class);
	/**
	 * Determines the acceptable range of humidities from a bee's ideal humidity. Reused by butterflies.
	 */
	public static final IValueChromosome<ToleranceType> HUMIDITY_TOLERANCE = ForestryAlleles.REGISTRY.valueChromosome(forestry("humidity_tolerance"), ToleranceType.class);
	/**
	 * Whether diurnal bees can work during the night, or nocturnal bees can work during the day. Reused by butterflies.
	 */
	public static final IRegistryChromosome<IActivityType> ACTIVITY = ForestryAlleles.REGISTRY.registryChromosome(forestry("activity"), IActivityType.class);
	/**
	 * Whether this bee can work when the sky above its housing is obstructed.
	 */
	public static final IBooleanChromosome CAVE_DWELLING = ForestryAlleles.REGISTRY.booleanChromosome(forestry("cave_dwelling"));
	/**
	 * Whether this bee can work while it is raining.
	 */
	public static final IBooleanChromosome TOLERATES_RAIN = ForestryAlleles.REGISTRY.booleanChromosome(forestry("tolerates_rain"));
	/**
	 * The type of flowers this bee needs to work. Also includes flowers that a bee can plant.
	 */
	public static final IRegistryChromosome<IFlowerType> FLOWER_TYPE = ForestryAlleles.REGISTRY.registryChromosome(forestry("flower_type"), IFlowerType.class);
	/**
	 * Determines the effect of a bee species. Its range is determined by {@link #TERRITORY}.
	 */
	public static final IRegistryChromosome<IBeeEffect> EFFECT = ForestryAlleles.REGISTRY.registryChromosome(forestry("bee_effect"), IBeeEffect.class);
	/**
	 * Determines how fast the hive can pollinate trees and plant flowers. Range is determined by {@link #TERRITORY}.
	 */
	public static final IIntegerChromosome POLLINATION = ForestryAlleles.REGISTRY.intChromosome(forestry("pollination"));
	/**
	 * Determines the area in which a bee can pollinate trees, grow flowers, and use its special effect.
	 */
	public static final IValueChromosome<Vec3i> TERRITORY = ForestryAlleles.REGISTRY.valueChromosome(forestry("territory"), Vec3i.class);
}
