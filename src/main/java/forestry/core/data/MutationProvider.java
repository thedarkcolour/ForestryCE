package forestry.core.data;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import forestry.api.apiculture.ForestryBeeSpecies;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

import forestry.api.ForestryConstants;
import forestry.api.ForestryTags;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.core.data.builder.MutationRecipeBuilder;
import forestry.core.genetics.mutations.MutationRecipe;
import org.jetbrains.annotations.ApiStatus;

import static forestry.api.apiculture.ForestryBeeSpecies.*;
import static forestry.api.arboriculture.ForestryTreeSpecies.*;
import static forestry.api.lepidopterology.ForestryButterflySpecies.*;

/**
 * Generates the built-in mutations as {@code forestry:{bee,tree,butterfly}_mutation} recipe JSON. These faithfully
 * reproduce the mutations that used to be code-registered via {@code .addMutations(...)} on the species builders.
 * <p>
 * This is a standalone {@link DataProvider} (writing into the same {@code data/forestry/recipe/} tree the recipe
 * provider uses) rather than piggybacking on the shared {@code RecipeOutput}: as its own provider it owns its slice
 * of the data-generator {@code HashCache}, so mutation JSONs that stop being generated (e.g. when a per-result
 * counter shrinks) are deleted on the next run instead of lingering as orphaned files.
 */
public class MutationProvider implements DataProvider {
	private final PackOutput.PathProvider recipePathProvider;
	private final CompletableFuture<HolderLookup.Provider> lookupProvider;
	private final List<MutationRecipeBuilder> pending = new ArrayList<>();

	public MutationProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		this.recipePathProvider = output.createRegistryElementsPathProvider(Registries.RECIPE);
		this.lookupProvider = lookupProvider;
	}

	/**
	 * Add your mutations here. Make sure NOT to call the super constructor in your mod.
	 */
	protected void addMutations() {
		bees();
		trees();
		butterflies();
	}

	@Override
	public String getName() {
		return "Forestry Mutation Recipes";
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return this.lookupProvider.thenCompose(registries -> {
			this.pending.clear();
			addMutations();
			return flush(cache, registries);
		});
	}

	// chance in [0, 1]
	protected MutationRecipeBuilder add(ResourceLocation speciesTypeId, ResourceLocation first, ResourceLocation second, ResourceLocation result, float chance) {
		MutationRecipeBuilder builder = new MutationRecipeBuilder(speciesTypeId, first, second, result, chance);
		this.pending.add(builder);
		return builder;
	}

	protected MutationRecipeBuilder tree(ResourceLocation first, ResourceLocation second, ResourceLocation result, float chance) {
		return add(ForestrySpeciesTypes.TREE, first, second, result, chance);
	}

	protected MutationRecipeBuilder butterfly(ResourceLocation first, ResourceLocation second, ResourceLocation result, float chance) {
		return add(ForestrySpeciesTypes.BUTTERFLY, first, second, result, chance);
	}

	private CompletableFuture<?> flush(CachedOutput cache, HolderLookup.Provider registries) {
		Map<String, Integer> counters = new HashMap<>();
		List<CompletableFuture<?>> futures = new ArrayList<>();
		for (MutationRecipeBuilder builder : this.pending) {
			String base = folder(builder.getSpeciesTypeId()) + "/" + builder.getResultId().getPath();
			int n = counters.merge(base, 1, Integer::sum);
			ResourceLocation id = ForestryConstants.forestry(base + "_" + n);
			MutationRecipe recipe = builder.build(id);
			futures.add(DataProvider.saveStable(cache, registries, Recipe.CODEC, recipe, this.recipePathProvider.json(id)));
		}
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private static String folder(ResourceLocation speciesTypeId) {
		if (speciesTypeId.equals(ForestrySpeciesTypes.BEE)) {
			return "bee_mutation";
		} else if (speciesTypeId.equals(ForestrySpeciesTypes.TREE)) {
			return "tree_mutation";
		} else if (speciesTypeId.equals(ForestrySpeciesTypes.BUTTERFLY)) {
			return "butterfly_mutation";
		}
		throw new IllegalArgumentException("Unknown species type for mutation: " + speciesTypeId);
	}

	private void bees() {
		ResourceLocation[] overworldHiveBees = {FOREST, MARSHY, MEADOWS, MODEST, SAVANNA, TROPICAL, VALIANT, WINTRY, LUSH, AQUATIC};

		// Common
		for (int i = 0; i < overworldHiveBees.length; i++) {
			ResourceLocation firstParent = overworldHiveBees[i];
			for (int j = i + 1; j < overworldHiveBees.length; j++) {
				add(ForestrySpeciesTypes.BEE, firstParent, overworldHiveBees[j], COMMON, 0.15f);
			}
		}

		// Cultivated
		for (ResourceLocation secondParent : overworldHiveBees) {
			add(ForestrySpeciesTypes.BEE, COMMON, secondParent, CULTIVATED, 0.12f);
		}

		add(ForestrySpeciesTypes.BEE, COMMON, CULTIVATED, NOBLE, 0.1f);
		add(ForestrySpeciesTypes.BEE, NOBLE, CULTIVATED, MAJESTIC, 0.08f);
		add(ForestrySpeciesTypes.BEE, NOBLE, MAJESTIC, IMPERIAL, 0.08f);
		add(ForestrySpeciesTypes.BEE, COMMON, CULTIVATED, DILIGENT, 0.1f);
		add(ForestrySpeciesTypes.BEE, DILIGENT, CULTIVATED, UNWEARY, 0.08f);
		add(ForestrySpeciesTypes.BEE, DILIGENT, UNWEARY, INDUSTRIOUS, 0.08f);

		// Sinister
		for (ResourceLocation parent : new ResourceLocation[]{MODEST, TROPICAL}) {
			add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.CULTIVATED, parent, ForestryBeeSpecies.SINISTER, 0.6f).biome(BiomeTags.IS_NETHER);
		}

		// Fiendish
		for (ResourceLocation parent : new ResourceLocation[]{CULTIVATED, MODEST, TROPICAL}) {
			add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SINISTER, parent, ForestryBeeSpecies.FIENDISH, 0.4f).biome(BiomeTags.IS_NETHER);
		}

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SINISTER, ForestryBeeSpecies.FIENDISH, ForestryBeeSpecies.DEMONIC, 0.25f).biome(BiomeTags.IS_NETHER);

		// Frugal
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MODEST, ForestryBeeSpecies.SINISTER, ForestryBeeSpecies.FRUGAL, 0.16f).temperature(TemperatureType.HOT, TemperatureType.HELLISH).humidity(HumidityType.ARID);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MODEST, ForestryBeeSpecies.FIENDISH, ForestryBeeSpecies.FRUGAL, 0.1f).temperature(TemperatureType.HOT, TemperatureType.HELLISH).humidity(HumidityType.ARID);

		// Austere
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MODEST, ForestryBeeSpecies.FRUGAL, ForestryBeeSpecies.AUSTERE, 0.08f).temperature(TemperatureType.HOT, TemperatureType.HELLISH).humidity(HumidityType.ARID);

		add(ForestrySpeciesTypes.BEE, AUSTERE, TROPICAL, EXOTIC, 0.12f);
		add(ForestrySpeciesTypes.BEE, EXOTIC, TROPICAL, EDENIC, 0.08f);
		add(ForestrySpeciesTypes.BEE, MONASTIC, AUSTERE, SECLUDED, 0.12f);
		add(ForestrySpeciesTypes.BEE, MONASTIC, SECLUDED, HERMITIC, 0.08f);
		add(ForestrySpeciesTypes.BEE, HERMITIC, ENDED, SPECTRAL, 0.04f);
		add(ForestrySpeciesTypes.BEE, SPECTRAL, ENDED, PHANTASMAL, 0.02f);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.INDUSTRIOUS, ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.ICY, 0.12f).temperature(TemperatureType.ICY, TemperatureType.COLD);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.ICY, ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.GLACIAL, 0.08f).temperature(TemperatureType.ICY, TemperatureType.COLD);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MARSHY, ForestryBeeSpecies.NOBLE, ForestryBeeSpecies.MIRY, 0.15f).temperature(TemperatureType.WARM).humidity(HumidityType.DAMP);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MARSHY, ForestryBeeSpecies.MIRY, ForestryBeeSpecies.BOGGY, 0.09f).temperature(TemperatureType.WARM).humidity(HumidityType.DAMP);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SAVANNA, ForestryBeeSpecies.DILIGENT, ForestryBeeSpecies.ARGIL, 0.15f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.ARID);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SAVANNA, ForestryBeeSpecies.ARGIL, ForestryBeeSpecies.PRIDE, 0.09f).biome(ForestryTags.Biomes.SHATTERED_SAVANNA);

		add(ForestrySpeciesTypes.BEE, SAVANNA, COMMON, VINDICTIVE, 0.12f);
		add(ForestrySpeciesTypes.BEE, VINDICTIVE, CULTIVATED, VENGEFUL, 0.08f);
		add(ForestrySpeciesTypes.BEE, VINDICTIVE, VENGEFUL, AVENGING, 0.04f);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.STEADFAST, ForestryBeeSpecies.VALIANT, ForestryBeeSpecies.HEROIC, 0.06f).biome(BiomeTags.IS_FOREST);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.LUSH, ForestryBeeSpecies.VALIANT, ForestryBeeSpecies.VERDANT, 0.1f).cave();
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.LUSH, ForestryBeeSpecies.VERDANT, ForestryBeeSpecies.LUXURIANT, 0.08f).cave();
		add(ForestrySpeciesTypes.BEE, LUXURIANT, MONASTIC, KLEPTOPLASTIC, 0.12f);
		add(ForestrySpeciesTypes.BEE, KLEPTOPLASTIC, LUXURIANT, PHOTOSYNTHETIC, 0.08f);
		add(ForestrySpeciesTypes.BEE, KLEPTOPLASTIC, MONASTIC, PHOTOSYNTHETIC, 0.08f);
		add(ForestrySpeciesTypes.BEE, KLEPTOPLASTIC, PHOTOSYNTHETIC, AUTOTROPHIC, 0.04f);

		add(ForestrySpeciesTypes.BEE, AQUATIC, PIRATE, PRISMATIC, 0.08f);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.PIRATE, ForestryBeeSpecies.ENDED, ForestryBeeSpecies.ABYSSAL, 0.4f).cave();
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.AQUATIC, ForestryBeeSpecies.ENDED, ForestryBeeSpecies.ABYSSAL, 0.4f).cave();
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.PIRATE, ForestryBeeSpecies.SHULKING, ForestryBeeSpecies.ABYSSAL, 0.6f).cave();
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.AQUATIC, ForestryBeeSpecies.SHULKING, ForestryBeeSpecies.ABYSSAL, 0.6f).cave();

		add(ForestrySpeciesTypes.BEE, EMBITTERED, FIENDISH, SPITEFUL, 0.12f);
		add(ForestrySpeciesTypes.BEE, SPITEFUL, EMBITTERED, SEETHING, 0.08f);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.EMBITTERED, ForestryBeeSpecies.ENDED, ForestryBeeSpecies.WARPED, 0.4f).biome(ForestryTags.Biomes.WARPED_FOREST);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SPITEFUL, ForestryBeeSpecies.ENDED, ForestryBeeSpecies.WARPED, 0.4f).biome(ForestryTags.Biomes.WARPED_FOREST);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.EMBITTERED, ForestryBeeSpecies.SHULKING, ForestryBeeSpecies.WARPED, 0.4f).biome(ForestryTags.Biomes.WARPED_FOREST);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SPITEFUL, ForestryBeeSpecies.SHULKING, ForestryBeeSpecies.WARPED, 0.4f).biome(ForestryTags.Biomes.WARPED_FOREST);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.HERMITIC, ForestryBeeSpecies.SCULK, 0.06f).biome(ForestryTags.Biomes.DEEP_DARK);

		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.DILIGENT, ForestryBeeSpecies.RURAL, 0.12f).biome(Tags.Biomes.IS_PLAINS);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.RURAL, ForestryBeeSpecies.UNWEARY, ForestryBeeSpecies.FARMERLY, 0.1f).biome(Tags.Biomes.IS_PLAINS);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.FARMERLY, ForestryBeeSpecies.INDUSTRIOUS, ForestryBeeSpecies.AGRARIAN, 0.06f).biome(Tags.Biomes.IS_PLAINS);

		add(ForestrySpeciesTypes.BEE, ANACHRONE, STEADFAST, PRIMEVAL, 0.15f);
		add(ForestrySpeciesTypes.BEE, RELIC, STEADFAST, ANACHRONE, 0.1f);

		// Festive (secret, date-restricted)
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.FOREST, ForestryBeeSpecies.LEPORINE, 0.1f).dateRange(Month.MARCH.getValue(), 29, Month.APRIL.getValue(), 15);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.FOREST, ForestryBeeSpecies.MERRY, 0.1f).dateRange(Month.DECEMBER.getValue(), 21, Month.DECEMBER.getValue(), 27);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.TIPSY, 0.1f).dateRange(Month.DECEMBER.getValue(), 27, Month.JANUARY.getValue(), 2);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.SINISTER, ForestryBeeSpecies.COMMON, ForestryBeeSpecies.TRICKY, 0.1f).dateRange(Month.OCTOBER.getValue(), 15, Month.NOVEMBER.getValue(), 3);
		add(ForestrySpeciesTypes.BEE, ForestryBeeSpecies.RURAL, ForestryBeeSpecies.NOBLE, ForestryBeeSpecies.PATRIOTIC, 0.15f).dateRange(Month.JULY.getValue(), 1, Month.JULY.getValue(), 17);
	}

	private void trees() {
		tree(OAK, BIRCH, LIME, 0.15f);
		tree(LIME, OAK, SOUR_CHERRY, 0.10f);
		tree(SOUR_CHERRY, DARK_OAK, WALNUT, 0.10f);
		tree(WALNUT, LIME, CHESTNUT, 0.05f).temperature(TemperatureType.NORMAL, TemperatureType.NORMAL).humidity(HumidityType.NORMAL);
		tree(SOUR_CHERRY, OAK, PEAR, 0.10f);
		tree(PEAR, SOUR_CHERRY, PLUM, 0.05f).temperature(TemperatureType.NORMAL, TemperatureType.NORMAL).humidity(HumidityType.NORMAL);
		tree(PEAR, LIME, FEIJOA, 0.05f).temperature(TemperatureType.NORMAL, TemperatureType.WARM).humidity(HumidityType.NORMAL, HumidityType.DAMP);
		tree(LIME, BIRCH, ELM, 0.10f);
		tree(ELM, OAK, MAPLE, 0.05f);
		tree(ELM, LIME, BEECH, 0.05f);
		tree(ELM, BIRCH, POPLAR, 0.05f);
		tree(POPLAR, DARK_OAK, WILLOW, 0.10f).temperature(TemperatureType.NORMAL).humidity(HumidityType.DAMP);
		tree(LIME, CHERRY_VANILLA, DOGWOOD, 0.10f);
		tree(DOGWOOD, CHERRY_VANILLA, JACARANDA, 0.05f).temperature(TemperatureType.NORMAL, TemperatureType.WARM).humidity(HumidityType.NORMAL, HumidityType.DAMP);
		tree(DOGWOOD, TEAK, IPE, 0.05f).temperature(TemperatureType.WARM).humidity(HumidityType.DAMP);
		tree(SPRUCE, OAK, LARCH, 0.15f);
		tree(LARCH, SPRUCE, PINE, 0.10f);
		tree(LARCH, OAK, FIR, 0.10f);
		tree(PINE, FIR, MACROCARPA, 0.10f);
		tree(PINE, LARCH, SEQUOIA, 0.10f);
		tree(SEQUOIA, GINKGO, GIANT_SEQUOIA, 0.05f).temperature(TemperatureType.ICY, TemperatureType.COLD).humidity(HumidityType.NORMAL);
		tree(MACROCARPA, FIR, PEWEN, 0.05f).temperature(TemperatureType.ICY, TemperatureType.COLD).humidity(HumidityType.NORMAL);
		tree(MACROCARPA, PINE, KAURI, 0.05f).temperature(TemperatureType.ICY, TemperatureType.COLD).humidity(HumidityType.NORMAL);
		tree(JUNGLE, DARK_OAK, TEAK, 0.15f);
		tree(TEAK, JUNGLE, KAPOK, 0.10f);
		tree(TEAK, BIRCH, BALSA, 0.10f);
		tree(LIME, JUNGLE, ORANGE, 0.10f);
		tree(BALSA, TEAK, EBONY, 0.10f);
		tree(KAPOK, TEAK, GREENHEART, 0.05f);
		tree(ORANGE, LIME, LEMON, 0.10f);
		tree(EBONY, BALSA, ZEBRANO, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.DAMP);
		tree(EBONY, KAPOK, MAHOGANY, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.DAMP);
		tree(WALNUT, KAPOK, COCONUT, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.DAMP);
		tree(LEMON, KAPOK, PAPAYA, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.DAMP);
		tree(ACACIA_VANILLA, JUNGLE, CAMELTHORN, 0.15f);
		tree(CAMELTHORN, JUNGLE, PADAUK, 0.10f);
		tree(CAMELTHORN, DARK_OAK, COCOBOLO, 0.10f);
		tree(CAMELTHORN, ACACIA_VANILLA, WENGE, 0.10f);
		tree(COCOBOLO, CAMELTHORN, MAHOE, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.ARID);
		tree(PADAUK, WENGE, BAOBAB, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.ARID);
		tree(COCOBOLO, SOUR_CHERRY, DATE, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.ARID);
		tree(WENGE, SOUR_CHERRY, OLIVE, 0.05f).temperature(TemperatureType.WARM, TemperatureType.HOT).humidity(HumidityType.ARID);
	}

	private void butterflies() {
		butterfly(LATTICED_HEATH, BRIMSTONE, BOMBYX_MORI, 0.07f);
	}
}
