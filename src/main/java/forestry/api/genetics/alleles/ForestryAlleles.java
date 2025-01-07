package forestry.api.genetics.alleles;

import java.util.List;

import net.minecraft.core.Vec3i;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.apiculture.ForestryActivityTypes;
import forestry.api.apiculture.ForestryBeeEffects;
import forestry.api.apiculture.ForestryFlowerTypes;
import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.arboriculture.ForestryFruits;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.core.ToleranceType;
import forestry.api.lepidopterology.ForestryButterflyEffects;
import forestry.api.lepidopterology.ForestryCocoons;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;

/**
 * All alleles defined by base Forestry. Although the field names might seem to suggest that they're
 * only intended for a certain chromosome, allele instances do not have any meaning besides the values
 * they contain. For example, if you need a dominant allele with the value 10, feel free to use {@link #LIFESPAN_SHORTEST}
 * or even copy it to a differently named field even if not for a lifespan-related chromosome. Note that you might
 * have to add that allele to the chromosome's list of valid alleles in its IKaryotype, which can be done using
 * {@link forestry.api.plugin.ISpeciesTypeBuilder#setKaryotype} and {@link forestry.api.plugin.IKaryotypeBuilder#get}
 */
public class ForestryAlleles {
	public static final IAlleleManager REGISTRY = IForestryApi.INSTANCE.getAlleleManager();

	// Booleans (Most chromosomes do not permit the recessive alleles)
	public static final IBooleanAllele TRUE = REGISTRY.booleanAllele(true, true);
	public static final IBooleanAllele FALSE = REGISTRY.booleanAllele(false, true);
	public static final IBooleanAllele TRUE_RECESSIVE = REGISTRY.booleanAllele(true, false);
	public static final IBooleanAllele FALSE_RECESSIVE = REGISTRY.booleanAllele(false, false);

	public static final List<IBooleanAllele> DEFAULT_BOOLEANS = List.of(TRUE, FALSE);

	// Bee Lifespan
	public static final IIntegerAllele LIFESPAN_SHORTEST = REGISTRY.intAllele(10, true);
	public static final IIntegerAllele LIFESPAN_SHORTER = REGISTRY.intAllele(20, true);
	public static final IIntegerAllele LIFESPAN_SHORT = REGISTRY.intAllele(30, true);
	public static final IIntegerAllele LIFESPAN_SHORTENED = REGISTRY.intAllele(35, true);
	public static final IIntegerAllele LIFESPAN_NORMAL = REGISTRY.intAllele(40);
	public static final IIntegerAllele LIFESPAN_ELONGATED = REGISTRY.intAllele(45, true);
	public static final IIntegerAllele LIFESPAN_LONG = REGISTRY.intAllele(50);
	public static final IIntegerAllele LIFESPAN_LONGER = REGISTRY.intAllele(60);
	public static final IIntegerAllele LIFESPAN_LONGEST = REGISTRY.intAllele(70);

	public static final List<IIntegerAllele> DEFAULT_LIFESPANS = List.of(LIFESPAN_SHORTEST, LIFESPAN_SHORTER, LIFESPAN_SHORT, LIFESPAN_SHORTENED, LIFESPAN_NORMAL, LIFESPAN_ELONGATED, LIFESPAN_LONG, LIFESPAN_LONGER, LIFESPAN_LONGEST);

	// Fertility
	public static final IIntegerAllele FERTILITY_1 = REGISTRY.intAllele(1, true);
	public static final IIntegerAllele FERTILITY_2 = REGISTRY.intAllele(2, true);
	public static final IIntegerAllele FERTILITY_3 = REGISTRY.intAllele(3);
	public static final IIntegerAllele FERTILITY_4 = REGISTRY.intAllele(4);
	// Fertilities above 5 are only used by Butterflies
	public static final IIntegerAllele FERTILITY_5 = REGISTRY.intAllele(5);
	public static final IIntegerAllele FERTILITY_6 = REGISTRY.intAllele(6);
	public static final IIntegerAllele FERTILITY_7 = REGISTRY.intAllele(7);
	public static final IIntegerAllele FERTILITY_8 = REGISTRY.intAllele(8);
	public static final IIntegerAllele FERTILITY_9 = REGISTRY.intAllele(9);
	public static final IIntegerAllele FERTILITY_10 = REGISTRY.intAllele(10);

	public static final List<IIntegerAllele> DEFAULT_BEE_FERTILITIES = List.of(FERTILITY_1, FERTILITY_2, FERTILITY_3, FERTILITY_4);
	public static final List<IIntegerAllele> DEFAULT_BUTTERFLY_FERTILITIES = List.of(FERTILITY_1, FERTILITY_2, FERTILITY_3, FERTILITY_4, FERTILITY_5, FERTILITY_6, FERTILITY_7, FERTILITY_8, FERTILITY_9, FERTILITY_10);

	// Pollination
	public static final IIntegerAllele POLLINATION_SLOWEST = REGISTRY.intAllele(5, true);
	public static final IIntegerAllele POLLINATION_SLOWER = REGISTRY.intAllele(10);
	public static final IIntegerAllele POLLINATION_SLOW = REGISTRY.intAllele(15);
	public static final IIntegerAllele POLLINATION_AVERAGE = REGISTRY.intAllele(20);
	public static final IIntegerAllele POLLINATION_FAST = REGISTRY.intAllele(25);
	public static final IIntegerAllele POLLINATION_FASTER = REGISTRY.intAllele(30);
	public static final IIntegerAllele POLLINATION_FASTEST = REGISTRY.intAllele(35);
	public static final IIntegerAllele POLLINATION_MAXIMUM = REGISTRY.intAllele(99, true);

	public static final List<IIntegerAllele> DEFAULT_POLLINATIONS = List.of(POLLINATION_SLOWEST, POLLINATION_SLOWER, POLLINATION_SLOW, POLLINATION_AVERAGE, POLLINATION_FAST, POLLINATION_FASTER, POLLINATION_FASTEST, POLLINATION_MAXIMUM);

	// Tolerance
	public static final IValueAllele<ToleranceType> TOLERANCE_NONE = REGISTRY.valueAllele(ToleranceType.NONE, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_BOTH_1 = REGISTRY.valueAllele(ToleranceType.BOTH_1, true, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_BOTH_2 = REGISTRY.valueAllele(ToleranceType.BOTH_2, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_BOTH_3 = REGISTRY.valueAllele(ToleranceType.BOTH_3, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_BOTH_4 = REGISTRY.valueAllele(ToleranceType.BOTH_4, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_BOTH_5 = REGISTRY.valueAllele(ToleranceType.BOTH_5, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_UP_1 = REGISTRY.valueAllele(ToleranceType.UP_1, true, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_UP_2 = REGISTRY.valueAllele(ToleranceType.UP_2, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_UP_3 = REGISTRY.valueAllele(ToleranceType.UP_3, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_UP_4 = REGISTRY.valueAllele(ToleranceType.UP_4, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_UP_5 = REGISTRY.valueAllele(ToleranceType.UP_5, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_DOWN_1 = REGISTRY.valueAllele(ToleranceType.DOWN_1, true, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_DOWN_2 = REGISTRY.valueAllele(ToleranceType.DOWN_2, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_DOWN_3 = REGISTRY.valueAllele(ToleranceType.DOWN_3, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_DOWN_4 = REGISTRY.valueAllele(ToleranceType.DOWN_4, IAlleleNaming.TOLERANCE_NAMING);
	public static final IValueAllele<ToleranceType> TOLERANCE_DOWN_5 = REGISTRY.valueAllele(ToleranceType.DOWN_5, IAlleleNaming.TOLERANCE_NAMING);

	public static final List<IValueAllele<ToleranceType>> DEFAULT_TEMPERATURE_TOLERANCES = List.of(TOLERANCE_NONE, TOLERANCE_BOTH_1, TOLERANCE_BOTH_2, TOLERANCE_BOTH_3, TOLERANCE_BOTH_4, TOLERANCE_BOTH_5, TOLERANCE_UP_1, TOLERANCE_UP_2, TOLERANCE_UP_3, TOLERANCE_UP_4, TOLERANCE_UP_5, TOLERANCE_DOWN_1, TOLERANCE_DOWN_2, TOLERANCE_DOWN_3, TOLERANCE_DOWN_4, TOLERANCE_DOWN_5);
	public static final List<IValueAllele<ToleranceType>> DEFAULT_HUMIDITY_TOLERANCES = List.of(TOLERANCE_NONE, TOLERANCE_BOTH_1, TOLERANCE_BOTH_2, TOLERANCE_UP_1, TOLERANCE_UP_2, TOLERANCE_DOWN_1, TOLERANCE_DOWN_2);

	// Activity
	public static final IRegistryAllele<IActivityType> ACTIVITY_DIURNAL = REGISTRY.registryAllele(ForestryActivityTypes.DIURNAL, BeeChromosomes.ACTIVITY);
	public static final IRegistryAllele<IActivityType> ACTIVITY_NOCTURNAL = REGISTRY.registryAllele(ForestryActivityTypes.NOCTURNAL, BeeChromosomes.ACTIVITY);
	public static final IRegistryAllele<IActivityType> ACTIVITY_CREPUSCULAR = REGISTRY.registryAllele(ForestryActivityTypes.CREPUSCULAR, BeeChromosomes.ACTIVITY);
	public static final IRegistryAllele<IActivityType> ACTIVITY_METATURNAL = REGISTRY.registryAllele(ForestryActivityTypes.METATURNAL, BeeChromosomes.ACTIVITY);
	public static final IRegistryAllele<IActivityType> ACTIVITY_CATHEMERAL = REGISTRY.registryAllele(ForestryActivityTypes.CATHEMERAL, BeeChromosomes.ACTIVITY);

	// Territory
	public static final IValueAllele<Vec3i> TERRITORY_AVERAGE = REGISTRY.valueAllele(new Vec3i(9, 6, 9), IAlleleNaming.VEC3I_NAMING);
	public static final IValueAllele<Vec3i> TERRITORY_LARGE = REGISTRY.valueAllele(new Vec3i(11, 8, 11), IAlleleNaming.VEC3I_NAMING);
	public static final IValueAllele<Vec3i> TERRITORY_LARGER = REGISTRY.valueAllele(new Vec3i(13, 12, 13), IAlleleNaming.VEC3I_NAMING);
	public static final IValueAllele<Vec3i> TERRITORY_LARGEST = REGISTRY.valueAllele(new Vec3i(15, 13, 15), IAlleleNaming.VEC3I_NAMING);

	public static final List<IValueAllele<Vec3i>> DEFAULT_TERRITORIES = List.of(TERRITORY_AVERAGE, TERRITORY_LARGE, TERRITORY_LARGER, TERRITORY_LARGEST);

	// Flower Type
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_VANILLA = REGISTRY.registryAllele(ForestryFlowerTypes.VANILLA, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_NETHER = REGISTRY.registryAllele(ForestryFlowerTypes.NETHER, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_CACTI = REGISTRY.registryAllele(ForestryFlowerTypes.CACTI, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_MUSHROOMS = REGISTRY.registryAllele(ForestryFlowerTypes.MUSHROOMS, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_END = REGISTRY.registryAllele(ForestryFlowerTypes.END, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_JUNGLE = REGISTRY.registryAllele(ForestryFlowerTypes.JUNGLE, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_SNOW = REGISTRY.registryAllele(ForestryFlowerTypes.SNOW, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_WHEAT = REGISTRY.registryAllele(ForestryFlowerTypes.WHEAT, BeeChromosomes.FLOWER_TYPE);
	public static final IRegistryAllele<IFlowerType> FLOWER_TYPE_GOURD = REGISTRY.registryAllele(ForestryFlowerTypes.GOURD, BeeChromosomes.FLOWER_TYPE);

	// Bee Effect
	public static final IRegistryAllele<IBeeEffect> EFFECT_NONE = REGISTRY.registryAllele(ForestryBeeEffects.NONE, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_AGGRESSIVE = REGISTRY.registryAllele(ForestryBeeEffects.AGGRESSIVE, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_HEROIC = REGISTRY.registryAllele(ForestryBeeEffects.HEROIC, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_BEATIFIC = REGISTRY.registryAllele(ForestryBeeEffects.BEATIFIC, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_MIASMIC = REGISTRY.registryAllele(ForestryBeeEffects.MIASMIC, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_MISANTHROPE = REGISTRY.registryAllele(ForestryBeeEffects.MISANTHROPE, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_GLACIAL = REGISTRY.registryAllele(ForestryBeeEffects.GLACIAL, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_RADIOACTIVE = REGISTRY.registryAllele(ForestryBeeEffects.RADIOACTIVE, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_CREEPER = REGISTRY.registryAllele(ForestryBeeEffects.CREEPER, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_IGNITION = REGISTRY.registryAllele(ForestryBeeEffects.IGNITION, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_EXPLORATION = REGISTRY.registryAllele(ForestryBeeEffects.EXPLORATION, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_EASTER = REGISTRY.registryAllele(ForestryBeeEffects.EASTER, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_SNOWING = REGISTRY.registryAllele(ForestryBeeEffects.SNOWING, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_DRUNKARD = REGISTRY.registryAllele(ForestryBeeEffects.DRUNKARD, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_REANIMATION = REGISTRY.registryAllele(ForestryBeeEffects.REANIMATION, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_RESURRECTION = REGISTRY.registryAllele(ForestryBeeEffects.RESURRECTION, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_REPULSION = REGISTRY.registryAllele(ForestryBeeEffects.REPULSION, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_FERTILE = REGISTRY.registryAllele(ForestryBeeEffects.FERTILE, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_MYCOPHILIC = REGISTRY.registryAllele(ForestryBeeEffects.MYCOPHILIC, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_SIFTER = REGISTRY.registryAllele(ForestryBeeEffects.SIFTER, BeeChromosomes.EFFECT);
	public static final IRegistryAllele<IBeeEffect> EFFECT_HAKUNA_MATATA = REGISTRY.registryAllele(ForestryBeeEffects.HAKUNA_MATATA, BeeChromosomes.EFFECT);
	//public static final IRegistryAllele<IBeeEffect> EFFECT_PATRIOTIC = Todos.todo();

	// Speed
	public static final IFloatAllele SPEED_SLOWEST = REGISTRY.floatAllele(0.3f, true);
	public static final IFloatAllele SPEED_SLOWER = REGISTRY.floatAllele(0.6f, true);
	public static final IFloatAllele SPEED_SLOW = REGISTRY.floatAllele(0.8f, true);
	public static final IFloatAllele SPEED_NORMAL = REGISTRY.floatAllele(1.0f);
	public static final IFloatAllele SPEED_FAST = REGISTRY.floatAllele(1.2f, true);
	public static final IFloatAllele SPEED_FASTER = REGISTRY.floatAllele(1.4f);
	public static final IFloatAllele SPEED_FASTEST = REGISTRY.floatAllele(1.7f);

	public static final List<IFloatAllele> DEFAULT_SPEEDS = List.of(SPEED_SLOWEST, SPEED_SLOWER, SPEED_SLOW, SPEED_NORMAL, SPEED_FAST, SPEED_FASTER, SPEED_FASTEST);

	// Size
	public static final IFloatAllele SIZE_SMALLEST = REGISTRY.floatAllele(0.3f);
	public static final IFloatAllele SIZE_SMALLER = REGISTRY.floatAllele(0.4f);
	public static final IFloatAllele SIZE_SMALL = REGISTRY.floatAllele(0.5f);
	public static final IFloatAllele SIZE_AVERAGE = REGISTRY.floatAllele(0.6f);
	public static final IFloatAllele SIZE_LARGE = REGISTRY.floatAllele(0.75f);
	public static final IFloatAllele SIZE_LARGER = REGISTRY.floatAllele(0.9f);
	public static final IFloatAllele SIZE_LARGEST = REGISTRY.floatAllele(1.0f);

	public static final List<IFloatAllele> DEFAULT_SIZES = List.of(SIZE_SMALLEST, SIZE_SMALLER, SIZE_SMALL, SIZE_AVERAGE, SIZE_LARGE, SIZE_LARGER, SIZE_LARGEST);

	// Metabolism
	public static final IIntegerAllele METABOLISM_SLOWEST = REGISTRY.intAllele(1);
	public static final IIntegerAllele METABOLISM_SLOWER = REGISTRY.intAllele(2);
	public static final IIntegerAllele METABOLISM_SLOW = REGISTRY.intAllele(3);
	public static final IIntegerAllele METABOLISM_NORMAL = REGISTRY.intAllele(5);
	public static final IIntegerAllele METABOLISM_FAST = REGISTRY.intAllele(7);
	public static final IIntegerAllele METABOLISM_FASTER = REGISTRY.intAllele(8);
	public static final IIntegerAllele METABOLISM_FASTEST = REGISTRY.intAllele(10);

	public static final List<IIntegerAllele> DEFAULT_METABOLISMS = List.of(METABOLISM_SLOWEST, METABOLISM_SLOWER, METABOLISM_SLOW, METABOLISM_NORMAL, METABOLISM_FAST, METABOLISM_FASTER, METABOLISM_FASTEST);

	// Butterfly Effect
	public static final IValueAllele<IButterflyEffect> BUTTERFLY_EFFECT_NONE = REGISTRY.registryAllele(ForestryButterflyEffects.NONE, ButterflyChromosomes.EFFECT);

	// Butterfly Cocoon
	public static final IRegistryAllele<IButterflyCocoon> COCOON_DEFAULT = REGISTRY.registryAllele(ForestryCocoons.DEFAULT, ButterflyChromosomes.COCOON);
	public static final IRegistryAllele<IButterflyCocoon> COCOON_SILK = REGISTRY.registryAllele(ForestryCocoons.SILK, ButterflyChromosomes.COCOON);

	// Sappiness
	public static final IFloatAllele SAPPINESS_LOWEST = REGISTRY.floatAllele(0.1f, true);
	public static final IFloatAllele SAPPINESS_LOWER = REGISTRY.floatAllele(0.2f, true);
	public static final IFloatAllele SAPPINESS_LOW = REGISTRY.floatAllele(0.3f, true);
	public static final IFloatAllele SAPPINESS_AVERAGE = REGISTRY.floatAllele(0.4f, true);
	public static final IFloatAllele SAPPINESS_HIGH = REGISTRY.floatAllele(0.6f, true);
	public static final IFloatAllele SAPPINESS_HIGHER = REGISTRY.floatAllele(0.8f);
	public static final IFloatAllele SAPPINESS_HIGHEST = REGISTRY.floatAllele(1.0f);

	public static final List<IFloatAllele> DEFAULT_SAPPINESSES = List.of(SAPPINESS_LOWEST, SAPPINESS_LOWER, SAPPINESS_LOW, SAPPINESS_AVERAGE, SAPPINESS_HIGH, SAPPINESS_HIGHER, SAPPINESS_HIGHEST);

	// Saplings
	public static final IFloatAllele SAPLINGS_LOWEST = REGISTRY.floatAllele(0.01f, true);
	public static final IFloatAllele SAPLINGS_LOWER = REGISTRY.floatAllele(0.025f, true);
	public static final IFloatAllele SAPLINGS_LOW = REGISTRY.floatAllele(0.035f, true);
	public static final IFloatAllele SAPLINGS_AVERAGE = REGISTRY.floatAllele(0.05f, true);
	public static final IFloatAllele SAPLINGS_HIGH = REGISTRY.floatAllele(0.1f, true);
	public static final IFloatAllele SAPLINGS_HIGHER = REGISTRY.floatAllele(0.2f, true);
	public static final IFloatAllele SAPLINGS_HIGHEST = REGISTRY.floatAllele(0.3f, true);

	public static final List<IFloatAllele> DEFAULT_SAPLINGS = List.of(SAPLINGS_LOWEST, SAPLINGS_LOWER, SAPLINGS_LOW, SAPLINGS_AVERAGE, SAPLINGS_HIGH, SAPLINGS_HIGHER, SAPLINGS_HIGHEST);

	// Fruits
	public static final IRegistryAllele<IFruit> FRUIT_NONE = REGISTRY.registryAllele(ForestryFruits.NONE, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_APPLE = REGISTRY.registryAllele(ForestryFruits.APPLE, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_COCOA = REGISTRY.registryAllele(ForestryFruits.COCOA, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_CHESTNUT = REGISTRY.registryAllele(ForestryFruits.CHESTNUT, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_WALNUT = REGISTRY.registryAllele(ForestryFruits.WALNUT, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_CHERRY = REGISTRY.registryAllele(ForestryFruits.CHERRY, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_DATES = REGISTRY.registryAllele(ForestryFruits.DATES, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_PAPAYA = REGISTRY.registryAllele(ForestryFruits.PAPAYA, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_LEMON = REGISTRY.registryAllele(ForestryFruits.LEMON, TreeChromosomes.FRUIT);
	public static final IRegistryAllele<IFruit> FRUIT_PLUM = REGISTRY.registryAllele(ForestryFruits.PLUM, TreeChromosomes.FRUIT);

	// Tree Effect
	public static final IValueAllele<ITreeEffect> TREE_EFFECT_NONE = REGISTRY.registryAllele(ForestryConstants.forestry("tree_effect_none"), TreeChromosomes.EFFECT);
	public static final IValueAllele<ITreeEffect> TREE_EFFECT_BLOSSOMING = REGISTRY.registryAllele(ForestryConstants.forestry("tree_effect_blossoming"), TreeChromosomes.EFFECT);

	// Maturation
	public static final IIntegerAllele MATURATION_SLOWEST = REGISTRY.intAllele(10, true);
	public static final IIntegerAllele MATURATION_SLOWER = REGISTRY.intAllele(7);
	public static final IIntegerAllele MATURATION_SLOW = REGISTRY.intAllele(5, true);
	public static final IIntegerAllele MATURATION_AVERAGE = REGISTRY.intAllele(4);
	public static final IIntegerAllele MATURATION_FAST = REGISTRY.intAllele(3);
	public static final IIntegerAllele MATURATION_FASTER = REGISTRY.intAllele(2);
	public static final IIntegerAllele MATURATION_FASTEST = REGISTRY.intAllele(1);

	public static final List<IIntegerAllele> DEFAULT_MATURATIONS = List.of(MATURATION_SLOWEST, MATURATION_SLOWER, MATURATION_SLOW, MATURATION_AVERAGE, MATURATION_FAST, MATURATION_FASTER, MATURATION_FASTEST);

	// Yield
	public static final IFloatAllele YIELD_LOWEST = REGISTRY.floatAllele(0.025f, true);
	public static final IFloatAllele YIELD_LOWER = REGISTRY.floatAllele(0.05f, true);
	public static final IFloatAllele YIELD_LOW = REGISTRY.floatAllele(0.1f, true);
	public static final IFloatAllele YIELD_AVERAGE = REGISTRY.floatAllele(0.2f, true);
	public static final IFloatAllele YIELD_HIGH = REGISTRY.floatAllele(0.3f);
	public static final IFloatAllele YIELD_HIGHER = REGISTRY.floatAllele(0.35f);
	public static final IFloatAllele YIELD_HIGHEST = REGISTRY.floatAllele(0.4f);

	public static final List<IFloatAllele> DEFAULT_YIELDS = List.of(YIELD_LOWEST, YIELD_LOWER, YIELD_LOW, YIELD_AVERAGE, YIELD_HIGH, YIELD_HIGHER, YIELD_HIGHEST);

	// Height
	public static final IFloatAllele HEIGHT_SMALLEST = REGISTRY.floatAllele(0.25f);
	public static final IFloatAllele HEIGHT_SMALLER = REGISTRY.floatAllele(0.5f);
	public static final IFloatAllele HEIGHT_SMALL = REGISTRY.floatAllele(0.75f);
	public static final IFloatAllele HEIGHT_AVERAGE = REGISTRY.floatAllele(1.0f);
	public static final IFloatAllele HEIGHT_LARGE = REGISTRY.floatAllele(1.25f);
	public static final IFloatAllele HEIGHT_LARGER = REGISTRY.floatAllele(1.5f);
	public static final IFloatAllele HEIGHT_LARGEST = REGISTRY.floatAllele(1.75f);
	public static final IFloatAllele HEIGHT_GIGANTIC = REGISTRY.floatAllele(2.0f);

	public static final List<IFloatAllele> DEFAULT_HEIGHTS = List.of(HEIGHT_SMALLEST, HEIGHT_SMALLER, HEIGHT_SMALL, HEIGHT_AVERAGE, HEIGHT_LARGE, HEIGHT_LARGER, HEIGHT_LARGEST, HEIGHT_GIGANTIC);

	// Girth
	public static final IIntegerAllele GIRTH_1 = REGISTRY.intAllele(1);
	public static final IIntegerAllele GIRTH_2 = REGISTRY.intAllele(2);
	public static final IIntegerAllele GIRTH_3 = REGISTRY.intAllele(3);
	public static final IIntegerAllele GIRTH_4 = REGISTRY.intAllele(4);
	public static final IIntegerAllele GIRTH_5 = REGISTRY.intAllele(5);
	public static final IIntegerAllele GIRTH_6 = REGISTRY.intAllele(6);
	public static final IIntegerAllele GIRTH_7 = REGISTRY.intAllele(7);
	public static final IIntegerAllele GIRTH_8 = REGISTRY.intAllele(8);
	public static final IIntegerAllele GIRTH_9 = REGISTRY.intAllele(9);
	public static final IIntegerAllele GIRTH_10 = REGISTRY.intAllele(10);

	public static final List<IIntegerAllele> DEFAULT_GIRTHS = List.of(GIRTH_1, GIRTH_2, GIRTH_3, GIRTH_4, GIRTH_5, GIRTH_6, GIRTH_7, GIRTH_8, GIRTH_9, GIRTH_10);
}
