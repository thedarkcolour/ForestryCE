package forestry.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.ForestryCapabilities;
import forestry.api.ForestryConstants;
import forestry.api.ForestryRegistries;
import forestry.api.IForestryApi;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IForestryModule;
import forestry.api.modules.IPacketRegistry;
import forestry.core.engine.genetics.FlowerTypeManager;
import forestry.core.engine.genetics.TaxonManager;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.platform.block.TileStreamUpdateTracker;
import forestry.core.platform.client.CoreClientHandler;
import forestry.core.engine.climate.ForestryClimateManager;
import forestry.core.platform.commands.DiagnosticsCommand;
import forestry.core.platform.commands.DumpCommand;
import forestry.core.features.CoreItems;
import forestry.core.features.CoreTiles;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.content.tools.ItemPipette;
import forestry.core.content.tools.ItemSpectacles;
import forestry.core.platform.loot.ConditionLootModifier;
import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.network.PacketIdServer;
import forestry.core.platform.network.packets.*;
import forestry.core.platform.owner.GameProfileDataSerializer;
import forestry.core.platform.recipes.RecipeManagers;
import forestry.core.platform.util.NetworkUtil;
import forestry.modules.BlankForestryModule;
import forestry.modules.ForestryModuleManager;
import forestry.modules.ModuleUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.function.Consumer;
import forestry.core.platform.PickupHandlerCore;

@ForestryModule
public class ModuleCore extends BlankForestryModule {
	private static volatile boolean apiInitialized = false;

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.CORE;
	}

	private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
		DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, ForestryConstants.MOD_ID);

	static {
		ENTITY_DATA_SERIALIZERS.register("game_profile", () -> GameProfileDataSerializer.INSTANCE);
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		ENTITY_DATA_SERIALIZERS.register(modBus);
		modBus.addListener(ModuleCore::onCommonSetup);
		modBus.addListener(ModuleCore::registerCapabilities);
		modBus.addListener(ModuleCore::registerGlobalLootModifiers);
		modBus.addListener(ModuleCore::registerForestryRegistries);
		modBus.addListener(ModuleCore::onGatherData);

		ModuleUtil.loadFeatureProviders();
		NeoForge.EVENT_BUS.addListener(ModuleCore::onItemPickup);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onLevelTick);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onTagsUpdated);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onAddReloadListeners);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerCommands);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onDatapackSync);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ModuleCore::ensureApiInitialized);
	}

	private static void registerForestryRegistries(NewRegistryEvent event) {
		event.register(ForestryRegistries.CIRCUIT);
		event.register(ForestryRegistries.POSTAL_CARRIER);
		event.register(ForestryRegistries.SPECIES_TYPE);
		event.register(ForestryRegistries.BEE_EFFECT_TYPE);
		event.register(ForestryRegistries.MUTATION_CONDITION_TYPE);
		event.register(ForestryRegistries.PRODUCT_TYPE);
		event.register(ForestryRegistries.FLUID_PRODUCT_TYPE);
		event.register(ForestryRegistries.FLOWER_TYPE_SERIALIZER);
	}

	private static void onGatherData(net.neoforged.neoforge.data.event.GatherDataEvent event) {
		// Datagen skips FMLCommonSetupEvent, so initialize the API here so data
		// providers can resolve TreeManager / BeeManager / ButterflyManager.
		ensureApiInitialized();
	}

	/** todo remove
	 * Idempotent bootstrap of Forestry's runtime API. Some client-side events
	 * (e.g. ModelEvent.RegisterGeometryLoaders) fire before FMLCommonSetupEvent
	 * is processed and need TreeManager / BeeManager / etc. already wired up.
	 * Safe to call from any post-RegisterEvent context.
	 */
	public static synchronized void ensureApiInitialized() {
		if (apiInitialized) {
			return;
		}
		postItemRegistry();
		((ForestryModuleManager) IForestryApi.INSTANCE.getModuleManager()).setupApi();
		apiInitialized = true;
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(ForestryCapabilities.SPECTACLE_VISION, (stack, context) -> ItemSpectacles.SPECTACLE_VISION, CoreItems.SPECTACLES.item());
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> ((ItemPipette) stack.getItem()).createFluidHandler(stack), CoreItems.PIPETTE.item());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CoreTiles.ANALYZER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CoreTiles.ESCRITOIRE.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CoreTiles.APIARIST_CHEST.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CoreTiles.ARBORIST_CHEST.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CoreTiles.LEPIDOPTERIST_CHEST.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CoreTiles.ANALYZER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CoreTiles.ANALYZER.tileType(), (tile, side) -> tile.getTankManager());
	}

	private static void registerGlobalLootModifiers(RegisterEvent event) {
		event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> {
			helper.register(ForestryConstants.forestry("condition_modifier"), ConditionLootModifier.CODEC);
		});
	}

	private static void postItemRegistry() {
		// Modules load in dependency order (see ForestryModuleManager). A module that supplies one of
		// the api managers installs it here, over the no-op base put there at construction.
		for (IForestryModule module : IForestryApi.INSTANCE.getModuleManager().getLoadedModules()) {
			module.applyPluginRegistration();
		}
	}

	private static void onItemPickup(ItemEntityPickupEvent.Post event) {
		PickupHandlerCore.onItemPickup(event.getPlayer(), event.getItemEntity());
	}

	private static void onLevelTick(LevelTickEvent.Post event) {
		var server = event.getLevel().getServer();
		if (server != null) {
			TileStreamUpdateTracker.syncVisualUpdates(server);
		}
	}

	private static void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.shouldUpdateStaticData()) {
			event.getRegistryAccess().registry(Registries.BIOME).ifPresent(registry -> ((ForestryClimateManager) IForestryApi.INSTANCE.getClimateManager()).onBiomesReloaded(registry));
		}
	}

	private static void onAddReloadListeners(AddReloadListenerEvent event) {
		event.addListener((prepBarrier, resourceManager, prepProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			return prepBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
				RecipeManagers.invalidateCaches();
				NetworkUtil.sendToAllPlayers(new RecipeCachePacket());
			});
		});

		// Load datapack taxa from the "taxon" folder and merge them onto the code-registered taxonomy. Registered
		// before any module's species loader: a species' genus is resolved to a taxon as it is projected, so taxa
		// must exist first.
		event.addListener(TaxonManager.INSTANCE);
		// Flower types are shared by bees and butterflies, so base loads them. They must be in place
		// before any pollinating species is projected, which is why this sits with the taxa rather
		// than in apiculture's listener
		event.addListener(FlowerTypeManager.INSTANCE);

		// Modules load in dependency order (see ForestryModuleManager), which is also the order their data
		// depends on: core's taxa, then apiculture's flower types/effects/species, then arboriculture's
		// trees, then lepidopterology's butterflies. Apply order follows registration order.
		// todo check whether an event with explicit ordering phases would be better than leaning on module
		//  dependency order here. It works, but the coupling is implicit: a module that needs another
		//  module's data loaded first has to express that as a load-order dependency even when it has no
		//  other reason to depend on it.
		for (IForestryModule module : IForestryApi.INSTANCE.getModuleManager().getLoadedModules()) {
			module.registerReloadListeners(event);
		}

		// Rebuild each species type's mutation index from the (re)loaded mutation recipes. Mod reload listeners run
		// after vanilla ones (and the reload barrier applies listeners in order), so by the apply phase the vanilla
		// RecipeManager is fully populated. Run on the game executor since this mutates shared species-type state.
		RecipeManager recipeManager = event.getServerResources().getRecipeManager();
		event.addListener((prepBarrier, resourceManager, prepProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			return prepBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> GeneticsReloadHandler.rebuildMutations(recipeManager), gameExecutor);
		});
	}

	/**
	 * Sends core's taxa to the client on login/reload, then lets each module send its own definitions, before tags
	 * and recipes sync (per {@code OnDatapackSyncEvent}'s contract). The client has no datapack access, so these
	 * packets are its only source for the reloadable genetics data, and each species packet's {@code handle}
	 * rebuilds the client-side species (and, in order, mutation) index from them.
	 * <p>
	 * Taxa go first because species projection resolves each species' genus against them. The modules then run in
	 * load order, which is the order their data depends on - the same guarantee {@link #onAddReloadListeners}
	 * rests on.
	 */
	private static void onDatapackSync(OnDatapackSyncEvent event) {
		TaxonSyncPacket taxonPacket = new TaxonSyncPacket(TaxonManager.INSTANCE.getDefinitions());
		FlowerTypeSyncPacket flowerTypePacket = new FlowerTypeSyncPacket(FlowerTypeManager.INSTANCE.getDefinitions());
		event.getRelevantPlayers().forEach(player -> {
			NetworkUtil.sendToPlayer(taxonPacket, player);
			NetworkUtil.sendToPlayer(flowerTypePacket, player);
		});

		for (IForestryModule module : IForestryApi.INSTANCE.getModuleManager().getLoadedModules()) {
			module.syncDatapack(event);
		}
	}

	private static void registerCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> forestryCommand = LiteralArgumentBuilder.literal("forestry");

		forestryCommand.then(DiagnosticsCommand.register());
		forestryCommand.then(DumpCommand.register());
		forestryCommand.then(forestry.core.platform.commands.MultiblockDebugCommand.register());

		for (IForestryModule module : IForestryApi.INSTANCE.getModuleManager().getModulesForMod(ForestryConstants.MOD_ID)) {
			if (module instanceof BlankForestryModule forestryModule) {
				forestryModule.addToRootCommand(forestryCommand);
			}
		}

		event.getDispatcher().register(forestryCommand);
	}

	@Override
	public void applyPluginRegistration() {
		PluginManager.registerCircuits();
		PluginManager.registerGenetics();
		PluginManager.registerPollen();
	}

	@Override
	public boolean isCore() {
		return true;
	}

	@Override
	public List<ResourceLocation> getModuleDependencies() {
		return List.of();
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.GUI_SELECTION_REQUEST, PacketGuiSelectRequest::encode, PacketGuiSelectRequest::decode, PacketGuiSelectRequest::handle);
		registry.serverbound(PacketIdServer.PIPETTE_CLICK, PacketPipetteClick::encode, PacketPipetteClick::decode, PacketPipetteClick::handle);
		registry.serverbound(PacketIdServer.CHIPSET_CLICK, PacketChipsetClick::encode, PacketChipsetClick::decode, PacketChipsetClick::handle);
		registry.serverbound(PacketIdServer.SOLDERING_IRON_CLICK, PacketSolderingIronClick::encode, PacketSolderingIronClick::decode, PacketSolderingIronClick::handle);

		registry.clientbound(PacketIdClient.ERROR_UPDATE, PacketErrorUpdate::encode, PacketErrorUpdate::decode, PacketErrorUpdate::handle);
		registry.clientbound(PacketIdClient.GUI_UPDATE, PacketGuiStream::encode, PacketGuiStream::decode, PacketGuiStream::handle);
		registry.clientbound(PacketIdClient.GUI_LAYOUT_SELECT, PacketGuiLayoutSelect::encode, PacketGuiLayoutSelect::decode, PacketGuiLayoutSelect::handle);
		registry.clientbound(PacketIdClient.GUI_ENERGY, PacketGuiEnergy::encode, PacketGuiEnergy::decode, PacketGuiEnergy::handle);
		registry.clientbound(PacketIdClient.SOCKET_UPDATE, PacketSocketUpdate::encode, PacketSocketUpdate::decode, PacketSocketUpdate::handle);
		registry.clientbound(PacketIdClient.TILE_FORESTRY_UPDATE, PacketTileStream::encode, PacketTileStream::decode, PacketTileStream::handle);
		registry.clientbound(PacketIdClient.TILE_FORESTRY_ACTIVE, PacketActiveUpdate::encode, PacketActiveUpdate::decode, PacketActiveUpdate::handle);
		registry.clientbound(PacketIdClient.ITEMSTACK_DISPLAY, PacketItemStackDisplay::encode, PacketItemStackDisplay::decode, PacketItemStackDisplay::handle);
		registry.clientbound(PacketIdClient.GENOME_TRACKER_UPDATE, PacketGenomeTrackerSync::encode, PacketGenomeTrackerSync::decode, PacketGenomeTrackerSync::handle);
		registry.clientbound(PacketIdClient.TANK_LEVEL_UPDATE, PacketTankLevelUpdate::encode, PacketTankLevelUpdate::decode, PacketTankLevelUpdate::handle);
		registry.clientbound(PacketIdClient.RECIPE_CACHE, RecipeCachePacket::encode, RecipeCachePacket::decode, RecipeCachePacket::handle);
		registry.clientbound(PacketIdClient.REFRACTORY_WAX_ON, PacketRefractoryWax::encode, PacketRefractoryWax::decode, PacketRefractoryWax::handle);
		registry.clientbound(PacketIdClient.TAXON_SYNC, TaxonSyncPacket::encode, TaxonSyncPacket::decode, TaxonSyncPacket::handle);
		registry.clientbound(PacketIdClient.FLOWER_TYPE_SYNC, FlowerTypeSyncPacket::encode, FlowerTypeSyncPacket::decode, FlowerTypeSyncPacket::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new CoreClientHandler());
	}
}
