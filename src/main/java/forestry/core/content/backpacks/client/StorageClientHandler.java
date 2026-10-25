package forestry.core.content.backpacks.client;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.gui.NaturalistInventoryScreen;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.backpacks.features.BackpackMenuTypes;
import forestry.core.content.backpacks.gui.NaturalistBackpackMenu;
import forestry.core.content.backpacks.gui.BackpackScreen;
import forestry.core.content.backpacks.items.BackpackItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class StorageClientHandler implements IClientModuleHandler {
	// Standalone models resolve through ModelBakery.MODEL_LISTER ("models/<path>.json"),
	// without the legacy "item/" prefix that vanilla item lookups added in pre-1.21
	// versions. Use the explicit "item/filled_crate" path so the loader finds
	// assets/forestry/models/item/filled_crate.json.
	public static final ModelResourceLocation FILLED_CRATE_MODEL = ModelResourceLocation.standalone(ForestryConstants.forestry("item/filled_crate"));

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(StorageClientHandler::registerAdditionalModels);
		modBus.addListener(StorageClientHandler::registerModelLoaders);
		modBus.addListener(StorageClientHandler::onModelBake);
		modBus.addListener(StorageClientHandler::onClientSetup);
		modBus.addListener(StorageClientHandler::registerBackpackItemProperties);
	}

	@SuppressWarnings("deprecation")
	private static void registerBackpackItemProperties(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ItemPropertyFunction itemPropertyFunction = (stack, clientLevel, holder, idk) -> BackpackItem.getMode(stack).ordinal();
			IFeatureRegistry registry = ModFeatureRegistry.get(ForestryModuleIds.STORAGE);
			for (DeferredHolder<Item, ? extends Item> entry : registry.getRegistry(Registries.ITEM).getEntries()) {
				if (entry.get() instanceof BackpackItem) {
					ItemProperties.register(entry.get(), ResourceLocation.withDefaultNamespace("mode"), itemPropertyFunction);
				}
			}
		});
	}

	private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		event.register(FILLED_CRATE_MODEL);
	}

	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register(ForestryConstants.forestry("filled_crate"), new FilledCrateModel.Loader());
	}

	private static void onModelBake(ModelEvent.BakingCompleted event) {
		FilledCrateModel.cachedBaseModel = null;
		FilledCrateModel.cachedTransforms = null;
		FilledCrateModel.cachedQuads = null;
	}

	private static void onClientSetup(RegisterMenuScreensEvent event) {
		event.register(BackpackMenuTypes.BACKPACK.menuType(), BackpackScreen::new);
		event.register(BackpackMenuTypes.NATURALIST_BACKPACK.menuType(), NaturalistInventoryScreen<NaturalistBackpackMenu>::new);
	}
}
