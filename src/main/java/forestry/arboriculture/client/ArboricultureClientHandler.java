package forestry.arboriculture.client;

import forestry.api.client.IClientModuleHandler;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.blocks.BlockDecorativeLeaves;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureTiles;
import forestry.arboriculture.models.*;
import forestry.core.models.ClientManager;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ArboricultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ArboricultureClientHandler::registerModelLoaders);
		modBus.addListener(ArboricultureClientHandler::onClientSetup);
		modBus.addListener(ArboricultureClientHandler::registerEntityRenderers);
		modBus.addListener(ArboricultureClientHandler::registerModelLayers);
		modBus.addListener(ArboricultureClientHandler::beforeResourceLoad);
	}

	private static void beforeResourceLoad(RegisterClientReloadListenersEvent event) {
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			Sheets.addWoodType(type.getWoodType());
		}
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ClientManager clientManager = ClientManager.INSTANCE;
			clientManager.registerModel(new ModelLeaves(), ArboricultureBlocks.LEAVES);
			clientManager.registerModel(new ModelDecorativeLeaves<>(BlockDecorativeLeaves.class), ArboricultureBlocks.LEAVES_DECORATIVE);
			clientManager.registerModel(new ModelDefaultLeaves(), ArboricultureBlocks.LEAVES_DEFAULT);
			clientManager.registerModel(new ModelDefaultLeavesFruit(), ArboricultureBlocks.LEAVES_DEFAULT_FRUIT);

			// fruit overlays require CUTOUT_MIPPED, even in Fast graphics
			ArboricultureBlocks.LEAVES_DEFAULT.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			ItemBlockRenderTypes.setRenderLayer(ArboricultureBlocks.LEAVES.block(), RenderType.cutoutMipped());
			ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			ArboricultureBlocks.LEAVES_DECORATIVE.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
			ItemBlockRenderTypes.setRenderLayer(ArboricultureBlocks.SAPLING_GE.block(), RenderType.cutout());
			ArboricultureBlocks.DOORS.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));

			ArboricultureBlocks.PODS.getBlocks().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
		});
	}

	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register("sapling_ge", new SaplingModelLoader());
	}

	private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ArboricultureEntities.BOAT.entityType(), ctx -> new ForestryBoatRenderer(ctx, false));
		event.registerEntityRenderer(ArboricultureEntities.CHEST_BOAT.entityType(), ctx -> new ForestryBoatRenderer(ctx, true));
		event.registerBlockEntityRenderer(ArboricultureTiles.SIGN.tileType(), SignRenderer::new);
		event.registerBlockEntityRenderer(ArboricultureTiles.HANGING_SIGN.tileType(), HangingSignRenderer::new);
	}

	private static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			event.registerLayerDefinition(ForestryBoatRenderer.createBoatModelLocation(type, false), BoatModel::createBodyModel);
			event.registerLayerDefinition(ForestryBoatRenderer.createBoatModelLocation(type, true), ChestBoatModel::createBodyModel);
			//event.registerLayerDefinition(ModelLayers.createSignModelName(type.getWoodType()), SignRenderer::createSignLayer);
			//event.registerLayerDefinition(ModelLayers.createHangingSignModelName(type.getWoodType()), HangingSignRenderer::createHangingSignLayer);
		}
	}
}
