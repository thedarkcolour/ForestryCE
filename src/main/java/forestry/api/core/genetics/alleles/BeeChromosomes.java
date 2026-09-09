package forestry.api.core.genetics.alleles;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.ToleranceType;
import forestry.api.core.genetics.ForestrySpeciesTypes;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.ApiStatus;

import static forestry.api.ForestryConstants.forestry;

/**
 * All chromosomes of the Forestry bee species type.
 */
public class BeeChromosomes {
	@ApiStatus.Internal
	private static final Lazy<IBeeSpeciesType> BEE_TYPE = Lazy.of(() -> IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.BEE, IBeeSpeciesType.class));

	/**
	 * The species of a bee. The genome stores the species' ID.
	 */
	public static final IChromosome<ResourceLocation> SPECIES = ChromosomeFactory.referenceChromosome(ForestrySpeciesTypes.BEE, BeeChromosomes::resolveSpeciesOrDefault, IBeeSpecies::isDominant);

	/**
	 * Determines a queen's production speed. Shows up as "worker" in the portable analyzer.
	 */
	public static final IChromosome<Float> SPEED = ChromosomeFactory.floatChromosome(forestry("speed"));
	/**
	 * Determines a queen's lifespan.
	 */
	public static final IChromosome<Integer> LIFESPAN = ChromosomeFactory.intChromosome(forestry("lifespan"));
	/**
	 * The number of drones given when a queen dies.
	 */
	public static final IChromosome<Integer> FERTILITY = ChromosomeFactory.intChromosome(forestry("fertility"));
	/**
	 * Determines the acceptable range of temperatures from a bee's ideal temperature. Reused by butterflies.
	 */
	public static final IChromosome<ToleranceType> TEMPERATURE_TOLERANCE = ChromosomeFactory.valueChromosome(forestry("temperature_tolerance"), ToleranceType.CODEC, ToleranceType::name);
	/**
	 * Determines the acceptable range of humidities from a bee's ideal humidity. Reused by butterflies.
	 */
	public static final IChromosome<ToleranceType> HUMIDITY_TOLERANCE = ChromosomeFactory.valueChromosome(forestry("humidity_tolerance"), ToleranceType.CODEC, ToleranceType::name);
	/**
	 * The activity type determines when this bee is awake. Builtin types are found in {@link forestry.api.apiculture.ForestryActivityTypes}.
	 */
	public static final IChromosome<ResourceLocation> ACTIVITY = ChromosomeFactory.referenceChromosome(forestry("activity"), id -> BEE_TYPE.get().getActivityType(id), IActivityType::isDominant);
	/**
	 * Whether this bee can work when the sky above its housing is obstructed.
	 */
	public static final IChromosome<Boolean> CAVE_DWELLING = ChromosomeFactory.booleanChromosome(forestry("cave_dwelling"));
	/**
	 * Whether this bee can work while it is raining.
	 */
	public static final IChromosome<Boolean> TOLERATES_RAIN = ChromosomeFactory.booleanChromosome(forestry("tolerates_rain"));
	/**
	 * The type of flowers this bee needs to work. Also includes flowers that a bee can plant.
	 *
	 * <p>Resolved through the base flower type manager rather than through the bee species type.
	 * ButterflyChromosomes.FLOWER_TYPE is this same chromosome, so a butterfly must be able to
	 * resolve it with no apiculture jar installed.
	 */
	public static final IChromosome<ResourceLocation> FLOWER_TYPE = ChromosomeFactory.referenceChromosome(forestry("flower_type"), id -> IForestryApi.INSTANCE.getFlowerTypeManager().getFlowerType(id), IFlowerType::isDominant);
	/**
	 * Determines the effect of a bee species. Its range is determined by {@link #TERRITORY}.
	 */
	public static final IChromosome<ResourceLocation> EFFECT = ChromosomeFactory.referenceChromosome(forestry("bee_effect"), id -> BEE_TYPE.get().getBeeEffect(id), IBeeEffect::isDominant);
	/**
	 * Determines how fast the hive can pollinate trees and plant flowers. Range is determined by {@link #TERRITORY}.
	 */
	public static final IChromosome<Integer> POLLINATION = ChromosomeFactory.intChromosome(forestry("pollination"));
	/**
	 * Determines the area in which a bee can pollinate trees, grow flowers, and use its special effect.
	 */
	public static final IChromosome<Vec3i> TERRITORY = ChromosomeFactory.valueChromosome(forestry("territory"), Vec3i.CODEC, v -> v.getX() + "_" + v.getY() + "_" + v.getZ());

	@ApiStatus.Internal
	private static IBeeSpecies resolveSpeciesOrDefault(ResourceLocation id) {
		IBeeSpeciesType type = BEE_TYPE.get();
		IBeeSpecies species = type.getSpeciesSafe(id);
		if (species != null) {
			return species;
		}
		return type.getDefaultSpecies();
	}
}
