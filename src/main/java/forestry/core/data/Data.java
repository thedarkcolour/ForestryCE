package forestry.core.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.data.models.ForestryBlockStateProvider;
import forestry.core.data.models.ForestryItemModelProvider;
import forestry.core.data.models.ForestryWoodModelProvider;
import forestry.modules.ForestryModuleManager;

import thedarkcolour.modkit.data.DataHelper;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Data {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		preDataGen();

		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		DataHelper dataHelper = new DataHelper(ForestryConstants.MOD_ID, event);
		CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

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

		generator.addProvider(event.includeServer(), new ForestryAdvancementProvider(output, lookup, existingFileHelper));
		generator.addProvider(event.includeServer(), new ForestryLootTableProvider(output));
		generator.addProvider(event.includeServer(), new ForestryLootModifierProvider(output));
		generator.addProvider(event.includeClient(), new ForestryBlockStateProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryWoodModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryItemModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ForestryAtlasProvider(output, existingFileHelper));
		generator.addProvider(event.includeServer(), new ForestryFeaturesProvider(output, lookup));
	}

	// Hack fix to make API work in data generation environment
	public static void preDataGen() {
		((ForestryModuleManager) IForestryApi.INSTANCE.getModuleManager()).setupApi();

		PluginManager.registerClient();
	}
}
