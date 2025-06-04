package forestry.core.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import forestry.api.ForestryConstants;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.data.models.ForestryBlockStateProvider;
import forestry.core.data.models.ForestryItemModelProvider;
import forestry.core.data.models.ForestryWoodModelProvider;
import forestry.core.data.recipe.ForestryRecipeProvider;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import thedarkcolour.modkit.data.DataHelper;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Data {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		// Hack fix to make API work in data generation environment
		PluginManager.registerClient();

		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		DataHelper dataHelper = new DataHelper(ForestryConstants.MOD_ID, event);
		CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

		dataHelper.createTags(Registries.BLOCK, ForestryBlockTagsProvider::addTags);
		dataHelper.createTags(Registries.ITEM, (tags, l) -> {
			ForestryItemTagsProvider.addTags(tags);
			ForestryBackpackTagProvider.addTags(tags);
		});
		dataHelper.createTags(Registries.BIOME, ForestryBiomeTagsProvider::addTags);
		dataHelper.createTags(Registries.FLUID, ForestryFluidTagsProvider::addTags);
		dataHelper.createTags(Registries.POINT_OF_INTEREST_TYPE, ForestryPoiTypeTagProvider::addTags);
		dataHelper.createTags(Registries.PAINTING_VARIANT, ForestryPaintingTagsProvider::addTags);
		dataHelper.createRecipes(ForestryRecipeProvider::addRecipes);
		dataHelper.createDamageTypes(ForestryDamageTypesProvider::addTypes);
		dataHelper.createItemModels(false, false, false, ForestryItemModels::addModels);

		generator.addProvider(event.includeServer(), new ForestryAdvancementProvider(output, registries, existingFileHelper));
		generator.addProvider(event.includeServer(), new ForestryLootTableProvider(output));
		generator.addProvider(event.includeServer(), new ForestryLootModifierProvider(output, registries));
		generator.addProvider(event.includeClient(), new ForestryBlockStateProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryWoodModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryItemModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryAtlasProvider(output, existingFileHelper));
		generator.addProvider(event.includeServer(), new ForestryFeaturesProvider(output, registries));
		generator.addProvider(event.includeClient(), new ForestryCuriosProvider(output, existingFileHelper, registries));
		generator.addProvider(event.includeServer(), new ForestryDataMapProvider(output, registries));
	}
}
