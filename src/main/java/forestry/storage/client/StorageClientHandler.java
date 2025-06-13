package forestry.storage.client;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.gui.GuiNaturalistInventory;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.storage.features.BackpackMenuTypes;
import forestry.storage.gui.NaturalistBackpackMenu;
import forestry.storage.gui.GuiBackpack;
import forestry.storage.items.ItemBackpack;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class StorageClientHandler implements IClientModuleHandler {
	public static final ModelResourceLocation FILLED_CRATE_MODEL = new ModelResourceLocation(ForestryConstants.forestry("filled_crate"), "inventory");

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(StorageClientHandler::registerAdditionalModels);
		modBus.addListener(StorageClientHandler::registerModelLoaders);
		modBus.addListener(StorageClientHandler::onModelBake);
		modBus.addListener(StorageClientHandler::onClientSetup);

		IFeatureRegistry registry = ModFeatureRegistry.get(ForestryModuleIds.STORAGE);

		registry.addRegistryListener(Registries.ITEM, () -> {
			@SuppressWarnings("deprecation")
			ItemPropertyFunction itemPropertyFunction = (stack, clientLevel, holder, idk) -> ItemBackpack.getMode(stack).ordinal();

			for (Holder<Item> entry : registry.getRegistry(Registries.ITEM).getEntries()) {
				if (entry.value() instanceof ItemBackpack backpack) {
					ItemProperties.register(backpack, ResourceLocation.withDefaultNamespace("mode"), itemPropertyFunction);
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
		event.register(BackpackMenuTypes.BACKPACK.menuType(), GuiBackpack::new);
		event.register(BackpackMenuTypes.NATURALIST_BACKPACK.menuType(), GuiNaturalistInventory<NaturalistBackpackMenu>::new);
	}
}
