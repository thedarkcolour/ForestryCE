package forestry.core.content.backpacks.client;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.core.platform.gui.GuiNaturalistInventory;
import forestry.core.content.backpacks.features.BackpackMenuTypes;
import forestry.core.content.backpacks.gui.ContainerNaturalistBackpack;
import forestry.core.content.backpacks.gui.GuiBackpack;
import forestry.core.content.backpacks.items.BackpackItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

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
			// all registered backpacks, so addon backpacks from IBackpackInterface get the property too
			for (Item item : BuiltInRegistries.ITEM) {
				if (item instanceof BackpackItem) {
					ItemProperties.register(item, ResourceLocation.withDefaultNamespace("mode"), itemPropertyFunction);
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
		event.register(BackpackMenuTypes.NATURALIST_BACKPACK.menuType(), GuiNaturalistInventory<ContainerNaturalistBackpack>::new);
	}
}
