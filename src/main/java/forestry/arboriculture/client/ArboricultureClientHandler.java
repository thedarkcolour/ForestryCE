/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.client;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import forestry.api.client.IClientModuleHandler;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.blocks.BlockDecorativeLeaves;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureTiles;
import forestry.arboriculture.models.ModelDecorativeLeaves;
import forestry.arboriculture.models.ModelDefaultLeaves;
import forestry.arboriculture.models.ModelDefaultLeavesFruit;
import forestry.arboriculture.models.ModelLeaves;
import forestry.arboriculture.models.SaplingModelLoader;
import forestry.core.models.ClientManager;

public class ArboricultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ArboricultureClientHandler::registerModelLoaders);
		modBus.addListener(ArboricultureClientHandler::onClientSetup);
		modBus.addListener(ArboricultureClientHandler::registerEntityRenderers);
		modBus.addListener(ArboricultureClientHandler::registerModelLayers);
	}

	@SuppressWarnings("deprecation")
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

			for (ForestryWoodType type : ForestryWoodType.VALUES) {
				Sheets.addWoodType(type.getWoodType());
			}
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
