package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.apiculture.bees.genetics.BeeSpeciesProvider;
import forestry.apiculture.plugin.DefaultBeeSpecies;
import forestry.apiimpl.plugin.PluginManager;
import forestry.arboriculture.plugin.DefaultTreeSpecies;
import forestry.arboriculture.trees.genetics.TreeSpeciesProvider;
import forestry.core.data.models.ForestryBlockStateProvider;
import forestry.core.data.models.ForestryItemModelProvider;
import forestry.core.data.models.ForestryWoodModelProvider;
import forestry.core.data.recipe.ForestryRecipeProvider;
import forestry.core.platform.util.SpeciesUtil;
import forestry.modules.ForestryModuleManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import thedarkcolour.modkit.data.DataHelper;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ForestryConstants.MOD_ID)
public class Data {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		preDataGen();

		// Content jars attach here through ServiceLoader rather than by naming a provider class directly,
		// so core need not import a content jar's types. Each provider compiles from the source set of the
		// jar it generates for, so the compile classpath enforces what this indirection only asks of it.
		// Sorted so the run is deterministic. Loaded first because core's own scope is the negation of
		// what these declare; their gather calls still come last, at the bottom of this method
		List<IForestryDataProvider> contentProviders = ServiceLoader.load(IForestryDataProvider.class).stream()
				.map(ServiceLoader.Provider::get)
				.sorted(Comparator.comparing(provider -> provider.getClass().getName()))
				.toList();

		DataGenerator generator = event.getGenerator();
		PackOutput output = DataRoots.of(event, DataRoots.CORE);
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		// Core takes everything the content jars do not, which is the safe direction: an id registered
		// outside a feature module still gets its item model, and it gets it in the jar that is always
		// installed. The same set scopes core's models and core's loot, so the two cannot come to disagree
		Set<ResourceLocation> contentOwned = JarModules.ownedIds(contentModules(contentProviders));
		DataHelper dataHelper = new DataHelper.Builder(ForestryConstants.MOD_ID, event)
				.packOutput(output)
				.entryFilter(id -> !contentOwned.contains(id))
				.build();
		CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

		// English is the one thing that is not partitioned. Every content jar requires base, so base
		// can hold every key, which is where the ten hand-written locales beside it already are and
		// where a translator can find all of them at once. Unfiltered, so it names content ids too.
		// dataHelper stays scoped because its other job is item models, and a model does have to ship
		// in the jar registering the item it belongs to
		new DataHelper.Builder(ForestryConstants.MOD_ID, event)
				.packOutput(output)
				.build()
				.createEnglish(true, ForestryEnglishProvider::addTranslations);
		dataHelper.createTags(Registries.BLOCK, ForestryBlockTagsProvider::addTags);
		dataHelper.createTags(Registries.ITEM, (tags, l) -> {
			ForestryItemTagsProvider.addTags(tags);
			ForestryBackpackTagProvider.addTags(tags);
		});
		dataHelper.createTags(Registries.BIOME, ForestryBiomeTagsProvider::addTags);
		dataHelper.createTags(Registries.FLUID, ForestryFluidTagsProvider::addTags);
		dataHelper.createTags(Registries.POINT_OF_INTEREST_TYPE, ForestryPoiTypeTagProvider::addTags);
		// painting_variant became a datapack registry in 1.21; the painting JSONs and
		// the placeable tag are shipped under src/main/resources/data/forestry/painting_variant/
		// and data/minecraft/tags/painting_variant/placeable.json respectively. A small
		// validator (below) fails datagen if a variant JSON is missing from the tag.
		generator.addProvider(event.includeServer(), new ForestryPaintingPlaceableValidator());
		dataHelper.createRecipes(ForestryRecipeProvider::addRecipes);
		dataHelper.createDamageTypes(ForestryDamageTypesProvider::addTypes);
		dataHelper.createItemModels(false, false, false, ForestryItemModels::addModels);

		generator.addProvider(event.includeServer(), new ForestryAdvancementProvider(output, lookup, existingFileHelper));
		generator.addProvider(event.includeServer(), new ForestryLootTableProvider(output, lookup, contentOwned));
		generator.addProvider(event.includeServer(), new ForestryLootModifierProvider(output, lookup));
		generator.addProvider(event.includeClient(), new ForestryBlockStateProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryWoodModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryItemModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryAtlasProvider(output, lookup, existingFileHelper));
		generator.addProvider(event.includeServer(), new ForestryFeaturesProvider(output, lookup));
		generator.addProvider(event.includeServer(), new TaxonProvider(output));
		generator.addProvider(event.includeServer(), new FlowerTypeProvider(output));
		generator.addProvider(event.includeServer(), SpeciesUtil.BEE_TYPE.get().createEffectProvider(output, lookup, DefaultBeeEffects.create()));
		generator.addProvider(event.includeServer(), SpeciesUtil.BEE_TYPE.get().createSpeciesProvider(output, lookup, DefaultBeeSpecies::register));
		generator.addProvider(event.includeServer(), SpeciesUtil.TREE_TYPE.get().createSpeciesProvider(output, lookup, DefaultTreeSpecies::register));
		generator.addProvider(event.includeServer(), new MutationProvider(output, lookup));
		generator.addProvider(event.includeServer(), new ForestryDataMapProvider(output, lookup));
		generator.addProvider(event.includeClient(), new ForestryCuriosProvider(output, existingFileHelper, lookup));

		// Last, so a content provider can read whatever core's providers seeded
		contentProviders.forEach(provider -> provider.gather(event));
	}

	/**
	 * @param contentProviders The content jars' entry points, in the order they were loaded
	 * @return Every module those jars ship
	 */
	private static Set<ResourceLocation> contentModules(List<IForestryDataProvider> contentProviders) {
		Set<ResourceLocation> union = new HashSet<>();
		for (IForestryDataProvider provider : contentProviders) {
			for (ResourceLocation moduleId : provider.moduleIds()) {
				// Two jars claiming one module would write the same lang keys and the same loot tables
				// into both, and load order would decide which the game reads
				if (!union.add(moduleId)) {
					throw new IllegalStateException("Module " + moduleId + " is claimed by more than one content jar");
				}
			}
		}
		return union;
	}

	// Hack fix to make API work in data generation environment
	public static void preDataGen() {
		((ForestryModuleManager) IForestryApi.INSTANCE.getModuleManager()).setupApi();

		PluginManager.registerClient();

		// Base Forestry's taxonomy now ships as datapack JSON (generated by TaxonProvider), loaded at real server start
		// via a reload listener - but a standalone data generator run never fires that reload cycle. A species' genus
		// must resolve to a taxon when the species is seeded below, so seed the live taxonomy from the same source
		// TaxonProvider generates its JSON from, before anything touches species.
		TaxonProvider.seedLiveTaxaForDatagen();

		// Flower types now come exclusively from the datapack JSON generated by FlowerTypeProvider, loaded at real
		// server start via a reload listener - but a standalone data generator run never fires that reload cycle.
		// Karyotype default-allele resolution for the FLOWER_TYPE chromosome (bee/butterfly species genome building,
		// below) requires the built-ins to already be registered, so seed them from the same source
		// FlowerTypeProvider generates its JSON from before anything touches species.
		FlowerTypeProvider.seedLiveFlowerTypesForDatagen();

		// Some built-in bee effects are likewise datapack-defined (generated by BeeEffectProvider) rather than
		// code-registered; the bee_effect chromosome default-allele resolution below needs them merged onto the
		// code-registered effects (already set by setupApi() above) before any bee species is built.
		DefaultBeeEffects.seedLiveBeeEffectsForDatagen();

		// Bee species now come exclusively from the datapack JSON generated by BeeSpeciesProvider, loaded at real
		// server start via a reload listener - but a standalone data generator run never fires that reload cycle.
		// Loot tables reference bees by id (LootTableHelper#beeLoot -> OrganismFunction.fromId), so they need no live
		// species. The one remaining consumer is the centrifuge recipe whose *result* is a concrete bee ItemStack
		// (ForestryRecipeProvider -> BEE_TYPE.createStack(CHRONOFUGE, DRONE)): a bee stack's identity is its full genome
		// component, so building the result materializes a live species. Until that recipe result also references the
		// bee by id (see the lazy id-template-stack follow-up), seed the live species type from the same
		// DefaultBeeSpecies source BeeSpeciesProvider generates its JSON from.
		BeeSpeciesProvider.seedLiveSpeciesForDatagen();

		// Tree species come from datapack JSON at real server start; datagen never fires that reload, so seed the
		// live tree type from the same DefaultTreeSpecies source any stack-baking provider/loot needs.
		TreeSpeciesProvider.seedLiveSpeciesForDatagen();
	}
}
