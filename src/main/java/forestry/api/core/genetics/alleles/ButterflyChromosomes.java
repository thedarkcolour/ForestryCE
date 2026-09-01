package forestry.api.core.genetics.alleles;

import forestry.api.IForestryApi;
import forestry.api.core.ToleranceType;
import forestry.api.core.genetics.ForestrySpeciesTypes;
import forestry.api.lepidopterology.ForestryButterflyEffects;
import forestry.api.lepidopterology.ForestryCocoons;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.ApiStatus;

import static forestry.api.ForestryConstants.forestry;

public class ButterflyChromosomes {
	@ApiStatus.Internal
	private static final Lazy<IButterflySpeciesType> TYPE = Lazy.of(() -> IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.BUTTERFLY, IButterflySpeciesType.class));

	/**
	 * The species of a butterfly. The genome stores the species' ID.
	 */
	public static final IChromosome<ResourceLocation> SPECIES = ChromosomeFactory.referenceChromosome(ForestrySpeciesTypes.BUTTERFLY, ButterflyChromosomes::resolveSpeciesOrDefault, IButterflySpecies::isDominant);

	/**
	 * Determines physical size of a butterfly.
	 */
	public static final IChromosome<Float> SIZE = ChromosomeFactory.floatChromosome(forestry("size"));
	/**
	 * Determines the flight speed of a butterfly.
	 */
	public static final IChromosome<Float> SPEED = BeeChromosomes.SPEED;
	/**
	 * Determines how long this butterfly will live.
	 */
	public static final IChromosome<Integer> LIFESPAN = ChromosomeFactory.intChromosome(forestry("butterfly_lifespan"));
	/**
	 * Determines the rate at which caterpillars destroy leaves and influences cocoon drops.
	 */
	public static final IChromosome<Integer> METABOLISM = ChromosomeFactory.intChromosome(forestry("metabolism"));
	/**
	 * Determines how likely this butterfly is to mate as well as how fast its nurseries and cocoons mature.
	 */
	public static final IChromosome<Integer> FERTILITY = BeeChromosomes.FERTILITY;
	/**
	 * Determines the acceptable range of temperatures from a butterfly's ideal temperature.
	 */
	public static final IChromosome<ToleranceType> TEMPERATURE_TOLERANCE = BeeChromosomes.TEMPERATURE_TOLERANCE;
	/**
	 * Determines the acceptable range of humidities from a butterfly's ideal humidity.
	 */
	public static final IChromosome<ToleranceType> HUMIDITY_TOLERANCE = BeeChromosomes.HUMIDITY_TOLERANCE;
	/**
	 * Whether diurnal butterflies can work during the night, or nocturnal butterflies (moths) can work during the day.
	 */
	public static final IChromosome<Boolean> NEVER_SLEEPS = ChromosomeFactory.booleanChromosome(forestry("never_sleeps"));
	/**
	 * Whether this butterfly can spawn or fly while it is raining.
	 */
	public static final IChromosome<Boolean> TOLERATES_RAIN = BeeChromosomes.TOLERATES_RAIN;
	/**
	 * Whether this butterfly is immune to fire/lava damage.
	 */
	public static final IChromosome<Boolean> FIREPROOF = ChromosomeFactory.booleanChromosome(forestry("fireproof"));
	/**
	 * Unimplemented.
	 */
	public static final IChromosome<ResourceLocation> FLOWER_TYPE = BeeChromosomes.FLOWER_TYPE;
	/**
	 * Unimplemented.
	 */
	public static final IChromosome<ResourceLocation> EFFECT = ChromosomeFactory.referenceChromosome(forestry("butterfly_effect"), ButterflyChromosomes::resolveEffectOrDefault, IButterflyEffect::isDominant);
	/**
	 * Used for silk moths (Bombyx Mori) to affect cocoon drops.
	 */
	public static final IChromosome<ResourceLocation> COCOON = ChromosomeFactory.referenceChromosome(forestry("cocoon"), ButterflyChromosomes::resolveCocoonOrDefault, IButterflyCocoon::isDominant);

	@ApiStatus.Internal
	private static IButterflyEffect resolveEffectOrDefault(ResourceLocation id) {
		IButterflySpeciesType type = TYPE.get();
		IButterflyEffect effect = type.getButterflyEffectSafe(id);
		if (effect != null) {
			return effect;
		}
		return type.getButterflyEffectSafe(ForestryButterflyEffects.NONE);
	}

	@ApiStatus.Internal
	private static IButterflyCocoon resolveCocoonOrDefault(ResourceLocation id) {
		IButterflySpeciesType type = TYPE.get();
		IButterflyCocoon cocoon = type.getCocoonSafe(id);
		if (cocoon != null) {
			return cocoon;
		}
		return type.getCocoonSafe(ForestryCocoons.DEFAULT);
	}

	@ApiStatus.Internal
	private static IButterflySpecies resolveSpeciesOrDefault(ResourceLocation id) {
		IButterflySpeciesType type = TYPE.get();
		IButterflySpecies species = type.getSpeciesSafe(id);
		if (species != null) {
			return species;
		}
		return type.getDefaultSpecies();
	}
}
