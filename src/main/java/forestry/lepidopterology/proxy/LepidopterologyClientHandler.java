package forestry.lepidopterology.proxy;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.render.ForestryModelLayers;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.lepidopterology.items.ItemButterflyGE;
import forestry.lepidopterology.render.ButterflyEntityRenderer;
import forestry.lepidopterology.render.ButterflyItemModel;
import forestry.lepidopterology.render.ButterflyModel;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class LepidopterologyClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(LepidopterologyClientHandler::setupRenderers);
		modBus.addListener(LepidopterologyClientHandler::setupLayers);
		modBus.addListener(LepidopterologyClientHandler::registerModelLoaders);

		ModFeatureRegistry.get(ForestryModuleIds.LEPIDOPTEROLOGY).addRegistryListener(Registries.ITEM, () -> {
			@SuppressWarnings("deprecation")
			ItemPropertyFunction itemPropertyFunction = (stack, clientLevel, holder, idk) -> ItemButterflyGE.getAge(stack);

			ItemProperties.register(LepidopterologyItems.COCOON_GE.get(), ForestryConstants.forestry("age"), itemPropertyFunction);
		});
	}

	public static void setupRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(LepidopterologyEntities.BUTTERFLY.entityType(), ButterflyEntityRenderer::new);
	}

	public static void setupLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ForestryModelLayers.BUTTERFLY_LAYER, ButterflyModel::createLayer);
	}

	public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register("butterfly_ge", new ButterflyItemModel.Loader());
	}
}
