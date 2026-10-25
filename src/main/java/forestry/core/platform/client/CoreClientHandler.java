package forestry.core.platform.client;

import forestry.core.features.CoreItems;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import forestry.api.ForestryConstants;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.client.IClientModuleHandler;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.client.arboriculture.ITreeClientManager;
import forestry.api.core.ISpectacleBlock;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.engine.circuits.SolderingIronScreen;
import forestry.core.platform.config.Constants;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.features.*;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.gui.*;
import forestry.core.content.analyzer.*;
import forestry.core.content.burnbarrel.BurnBarrelScreen;
import forestry.core.content.escritoire.*;
import forestry.core.platform.item.SpecialRendererBlockItem;
import forestry.core.platform.models.ClientManager;
import forestry.core.platform.models.FluidContainerModel;
import forestry.core.platform.models.ModelBlockCached;
import forestry.core.platform.particles.CoreParticles;
import forestry.core.platform.render.*;
import forestry.core.content.machines.*;
import forestry.core.platform.util.GeneticsUtil;
import forestry.core.platform.util.RenderUtil;
import forestry.core.platform.registration.FeatureFluid;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.features.EnergyBlockEntities;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.modules.ModuleUtil;
import forestry.core.content.backpacks.features.BackpackItems;
import forestry.core.content.backpacks.features.CrateItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.awt.*;
import java.util.OptionalDouble;

public class CoreClientHandler implements IClientModuleHandler {
	public static BlockEntityWithoutLevelRenderer bewlr;
	// Copied from RenderStateShard.java (just LINES but with NO_DEPTH_TEST)
	public static final RenderType RENDER_TYPE_LINES_XRAY = RenderType.create("lines_xray", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false, RenderType.CompositeState.builder()
		.setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
		.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
		.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
		.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
		.setOutputState(RenderStateShard.OUTLINE_TARGET)
		.setWriteMaskState(RenderStateShard.COLOR_WRITE)
		.setCullState(RenderStateShard.NO_CULL)
		.setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
		.createCompositeState(false));

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(CoreClientHandler::onClientSetup);
		modBus.addListener(CoreClientHandler::registerMenuScreens);
		modBus.addListener(CoreClientHandler::registerModelLoaders);
		modBus.addListener(CoreClientHandler::additionalBakedModels);
		modBus.addListener(CoreClientHandler::bakeModels);
		modBus.addListener(CoreClientHandler::setupLayers);
		modBus.addListener(CoreClientHandler::clientSetupRenderers);
		modBus.addListener(CoreClientHandler::registerReloadListeners);
		modBus.addListener(CoreClientHandler::registerBlockColors);
		modBus.addListener(CoreClientHandler::registerItemColors);
		modBus.addListener(CoreClientHandler::registerParticleFactory);
		modBus.addListener(CoreClientHandler::registerClientExtensions);
		NeoForge.EVENT_BUS.addListener(CoreClientHandler::onClientTick);
		// HIGH priority so the client mutation index is rebuilt before NORMAL-priority consumers (e.g. JEI, which can
		// start synchronously during RecipesUpdatedEvent on a dedicated-server connection) read it.
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, CoreClientHandler::onRecipesUpdated);

		ModuleUtil.getModBus(ForestryConstants.MOD_ID).addListener(EventPriority.HIGHEST, ((ForestryClientApiImpl) IForestryClientApi.INSTANCE)::initializeTextureManager);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			CoreBlocks.BASE.getList().forEach((block) -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));

			for (ForestryFluids fluid : ForestryFluids.values()) {
				ItemBlockRenderTypes.setRenderLayer(fluid.getFluid(), RenderType.translucent());
				ItemBlockRenderTypes.setRenderLayer(fluid.getFlowing(), RenderType.translucent());
			}

			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.PHOSPHOR_WALL_TORCH.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.PHOSPHOR_TORCH.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.TIN_CHAIN.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.PHOSPHOR_LANTERN.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.BURN_BARREL.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(EnergyBlocks.SOLAR_PANEL.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.PLYWOOD_SHEET.block(), RenderType.cutout());

			CoreBlocks.JUMBO_CANDLES.getList().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
			CoreBlocks.BIG_CANDLES.getList().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
			// Deviation from 1.20.1: these two were left on the solid layer there, which turns the transparent
			// pixels of the vanilla candle templates black. Vanilla registers its own candles as cutout
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.RAINBOW_CANDLE.block(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(CoreBlocks.REFRACTORY_CANDLE.block(), RenderType.cutout());
		});

		bewlr = new ForestryBewlr(Minecraft.getInstance().getBlockEntityRenderDispatcher());
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(CoreMenuTypes.PORTABLE_ANALYZER.menuType(), PortableAnalyzerScreen::new);
		event.register(CoreMenuTypes.ANALYZER.menuType(), AnalyzerScreen::new);
		event.register(CoreMenuTypes.NATURALIST_INVENTORY.menuType(), NaturalistInventoryScreen<NaturalistInventoryMenu>::new);
		event.register(CoreMenuTypes.ESCRITOIRE.menuType(), EscritoireScreen::new);
		event.register(CoreMenuTypes.BURN_BARREL.menuType(), BurnBarrelScreen::new);
		event.register(CoreMenuTypes.SOLDERING_IRON.menuType(), SolderingIronScreen::new);
	}

	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register(ResourceLocation.fromNamespaceAndPath(ForestryConstants.MOD_ID, "fluid_container"), FluidContainerModel.Loader.INSTANCE);

		// Client model registration depends on TreeManager / BeeManager / etc. being
		// initialized. RegisterGeometryLoaders fires before FMLCommonSetupEvent, so make
		// sure the API is bootstrapped before plugin client registration runs.
		forestry.core.ModuleCore.ensureApiInitialized();
		PluginManager.registerClient();
	}

	private static void additionalBakedModels(ModelEvent.RegisterAdditional event) {
		IBeeClientManager beeManager = IForestryClientApi.INSTANCE.getBeeManager();

		for (BeeLifeStage stage : BeeLifeStage.values()) {
			for (ResourceLocation location : beeManager.getAllModelLocations(stage)) {
				event.register(ModelResourceLocation.standalone(location));
			}
		}

		ITreeClientManager treeManager = IForestryClientApi.INSTANCE.getTreeManager();

		for (Pair<ResourceLocation, ResourceLocation> pair : treeManager.getAllSaplingModels()) {
			event.register(ModelResourceLocation.standalone(pair.getFirst()));
			event.register(ModelResourceLocation.standalone(pair.getSecond()));
		}
	}

	private static void bakeModels(ModelEvent.ModifyBakingResult event) {
		ClientManager.INSTANCE.onBakeModels(event);
	}

	private static void setupLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ForestryModelLayers.NATURALIST_CHEST_LAYER, NaturalistChestRenderer::createBodyLayer);
		event.registerLayerDefinition(ForestryModelLayers.MILL_LAYER, RainmakerRenderer::createBodyLayer);
		event.registerLayerDefinition(ForestryModelLayers.ENGINE_LAYER, EngineRenderer::createBodyLayer);
	}

	private static void clientSetupRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// Core
		event.registerBlockEntityRenderer(CoreTiles.ANALYZER.tileType(), AnalyzerRenderer::new);
		event.registerBlockEntityRenderer(CoreTiles.ESCRITOIRE.tileType(), EscritoireRenderer::new);
		// Apiculture/Arboriculture/Lepidopterology
		event.registerBlockEntityRenderer(CoreTiles.APIARIST_CHEST.tileType(), ctx -> new NaturalistChestRenderer(ctx, "apiaristchest"));
		event.registerBlockEntityRenderer(CoreTiles.ARBORIST_CHEST.tileType(), ctx -> new NaturalistChestRenderer(ctx, "arbchest"));
		event.registerBlockEntityRenderer(CoreTiles.LEPIDOPTERIST_CHEST.tileType(), ctx -> new NaturalistChestRenderer(ctx, "lepichest"));
		// Engine
		event.registerBlockEntityRenderer(EnergyBlockEntities.CLOCKWORK_ENGINE.tileType(), ctx -> new EngineRenderer(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_clock_"));
		event.registerBlockEntityRenderer(EnergyBlockEntities.BIOGAS_ENGINE.tileType(), ctx -> new EngineRenderer(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_bronze_"));
		event.registerBlockEntityRenderer(EnergyBlockEntities.PEAT_ENGINE.tileType(), ctx -> new EngineRenderer(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_copper_"));
		event.registerBlockEntityRenderer(EnergyBlockEntities.COMBUSTION_ENGINE.tileType(), ctx -> new EngineRenderer(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_iron_"));
		event.registerBlockEntityRenderer(EnergyBlockEntities.SOLAR_ENGINE.tileType(), ctx -> new EngineRenderer(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_tin_"));
		// Factory
		event.registerBlockEntityRenderer(FactoryBlockEntities.RAINMAKER.tileType(), ctx -> new RainmakerRenderer(ctx, Constants.TEXTURE_PATH_BLOCK + "/rainmaker_"));
	}

	private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(((ForestryTextureManager) IForestryClientApi.INSTANCE.getTextureManager()).getSpriteUploader());
		event.registerReloadListener(ColourProperties.INSTANCE);

		event.registerReloadListener((prepBarrier, resourceManager, prepProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			return prepBarrier.wait(Unit.INSTANCE).thenRunAsync(ModelBlockCached::clear, gameExecutor);
		});
	}

	private static void registerParticleFactory(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(CoreParticles.REFRACTORY_WAX.get(), RefractoryWaxParticle::new);
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		// Replaces the deprecated per-instance Item#initializeClient override on ItemBlockTesr.
		IClientItemExtensions tesrExtensions = new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return bewlr;
			}
		};
		for (SpecialRendererBlockItem<?> item : SpecialRendererBlockItem.getInstances()) {
			event.registerItem(tesrExtensions, item);
		}

		// Replaces the deprecated per-instance FluidType#initializeClient override on ForestryFluidType.
		for (ForestryFluids fluid : ForestryFluids.values()) {
			FluidType type = fluid.getFluid().getFluidType();
			if (!(type instanceof FeatureFluid.ForestryFluidType forestryType)) {
				continue;
			}
			event.registerFluidType(new IClientFluidTypeExtensions() {
				@Override
				public ResourceLocation getStillTexture() {
					return forestryType.getStillTexture();
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return forestryType.getFlowingTexture();
				}

				@Override
				public int getTintColor() {
					// forestryType.getColor() double-tints placed fluids, since the still/flowing textures
					// are already tinted; white leaves the texture's own color alone
					return 0xFFFFFFFF;
				}
			}, type);
		}
	}

	private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
		// Turf wears the grass texture and tints it by biome, the same way vanilla tints its grass block.
		// Deviation from 1.20.1: that tree named the two blocks inside ClientManager's shared block colour,
		// which every other forestry block reaches through IColoredBlock. Neither turf block is one, so they
		// get vanilla's own lambda here instead
		event.register((state, level, pos, tintIndex) -> level != null && pos != null
			? BiomeColors.getAverageGrassColor(level, pos)
			: GrassColor.getDefaultColor(), CoreBlocks.TURF_BLOCK.block(), CoreBlocks.TURF.block());

		// Packaging machine tank slices, tinted with the fluid colour of whatever they hold
		event.register(ClientManager.FORESTRY_BLOCK_COLOR, FactoryBlocks.PLAIN.blockArray());
	}

	private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		// Core
		event.register(ClientManager.FORESTRY_ITEM_COLOR, FluidsItems.CONTAINERS.itemArray());
		event.register(ClientManager.FORESTRY_ITEM_COLOR, CoreItems.PIPETTE.item());
		event.register(ClientManager.FORESTRY_ITEM_COLOR, CoreItems.HONEY_DROP.item());



		// Backpacks
		event.register(ClientManager.FORESTRY_ITEM_COLOR,
			BackpackItems.APIARIST_BACKPACK.item(),
			BackpackItems.ARBORIST_BACKPACK.item(),
			BackpackItems.LEPIDOPTERIST_BACKPACK.item(),
			BackpackItems.MINER_BACKPACK.item(),
			BackpackItems.MINER_BACKPACK_T_2.item(),
			BackpackItems.DIGGER_BACKPACK.item(),
			BackpackItems.DIGGER_BACKPACK_T_2.item(),
			BackpackItems.FORESTER_BACKPACK.item(),
			BackpackItems.FORESTER_BACKPACK_T_2.item(),
			BackpackItems.HUNTER_BACKPACK.item(),
			BackpackItems.HUNTER_BACKPACK_T_2.item(),
			BackpackItems.ADVENTURER_BACKPACK.item(),
			BackpackItems.ADVENTURER_BACKPACK_T_2.item(),
			BackpackItems.BUILDER_BACKPACK.item(),
			BackpackItems.BUILDER_BACKPACK_T_2.item(),
			BackpackItems.BREWER_BACKPACK.item(),
			BackpackItems.BREWER_BACKPACK_T_2.item()
		);

		// A turf item has no level to sample, so both show the default grass colour, the way vanilla's does
		event.register((stack, tintIndex) -> GrassColor.getDefaultColor(), CoreBlocks.TURF_BLOCK.item(), CoreBlocks.TURF.item());

		// Crates. The bee ones are registered by ApicultureClientHandler
		event.register(ClientManager.FORESTRY_ITEM_COLOR, CrateItems.CRATED_GRASS_BLOCK.item());

	}

	private static void onRecipesUpdated(RecipesUpdatedEvent event) {
		// Recipes sync to the client automatically; rebuild the client-side mutation index for JEI/analyzer display.
		// Species (BeeSpeciesSyncPacket) arrive separately and are rebuilt there, always before this fires.
		GeneticsReloadHandler.rebuildMutations(event.getRecipeManager());
	}

	private static void onClientTick(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			Minecraft minecraft = Minecraft.getInstance();
			Player player = minecraft.player;

			if (player != null) {
				if (GeneticsUtil.hasNaturalistEye(player)) {
					// Draw lines around pollinated leaves and wild hives
					PoseStack stack = event.getPoseStack();

					Vec3 cameraPos = event.getCamera().getPosition();

					RENDER_TYPE_LINES_XRAY.setupRenderState();

					MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
					VertexConsumer lines = buffers.getBuffer(RENDER_TYPE_LINES_XRAY);

					int renderDistance = minecraft.options.renderDistance().get();
					BlockPos playerPos = minecraft.player.blockPosition();
					ChunkPos playerChunkPos = new ChunkPos(playerPos);

					Color color = RenderUtil.getRainbowColor(minecraft.level.getGameTime(), event.getPartialTick().getGameTimeDeltaPartialTick(false));

					// Steady rainbow outline (pollinated leaves, assembled multiblock anchors)...
					float r = color.getRed() / 255f;
					float g = color.getGreen() / 255f;
					float b = color.getBlue() / 255f;
					// ...and a pulsing white outline for work-in-progress markers (unformed multiblock parts).
					float flashAlpha = RenderUtil.getFlashingAlpha(minecraft.level.getGameTime(), event.getPartialTick().getGameTimeDeltaPartialTick(false));

					// Iterate through all chunks in render distance
					for (int chunkX = playerChunkPos.x - renderDistance; chunkX <= playerChunkPos.x + renderDistance; chunkX++) {
						for (int chunkZ = playerChunkPos.z - renderDistance; chunkZ <= playerChunkPos.z + renderDistance; chunkZ++) {
							LevelChunk chunk = minecraft.level.getChunk(chunkX, chunkZ);

							// Get all block entities in the chunk
							for (BlockEntity be : chunk.getBlockEntities().values()) {
								if (be instanceof ISpectacleBlock naturalist && naturalist.isHighlighted(player)) {
									BlockPos pos = be.getBlockPos();

									boolean flashing = naturalist.usesFlashingHighlight(player);
									float cr = flashing ? 1.0F : r;
									float cg = flashing ? 1.0F : g;
									float cb = flashing ? 1.0F : b;
									float ca = flashing ? flashAlpha : 1.0F;

									stack.pushPose();
									// Translate the matrix stack to avoid floating point precision errors
									stack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

									// render at origin (inflate slightly to avoid weirdness with selection box)
									LevelRenderer.renderLineBox(stack, lines, -0.001, -0.001, -0.001, 1.001, 1.001, 1.001, cr, cg, cb, ca);

									stack.popPose();
								}
							}
						}
					}

					buffers.endBatch();
					RENDER_TYPE_LINES_XRAY.clearRenderState();
				}
			}
		}
	}
}
