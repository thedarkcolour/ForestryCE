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
import forestry.apiculture.features.ApicultureItems;
import forestry.apiimpl.plugin.PluginManager;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.blocks.TileStreamUpdateTracker;
import forestry.core.client.CoreClientHandler;
import forestry.core.climate.ForestryClimateManager;
import forestry.core.commands.DiagnosticsCommand;
import forestry.core.commands.DumpCommand;
import forestry.core.features.CoreDataComponents;
import forestry.core.features.CoreItems;
import forestry.core.items.ItemSpectacles;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.FluidHandlerItemForestry;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.core.network.packets.*;
import forestry.core.recipes.RecipeManagers;
import forestry.core.utils.ModUtil;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.modules.BlankForestryModule;
import forestry.modules.ModuleUtil;
import forestry.modules.features.FeatureItem;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.List;
import java.util.function.Consumer;

@ForestryModule
public class ModuleCore extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.CORE;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleCore::onCommonSetup);
		ModUtil.addRegistryListener(Registries.ITEM, ModuleCore::postItemRegistry);

		ModuleUtil.loadFeatureProviders();
		//NeoForge.EVENT_BUS.addListener(ModuleCore::onItemPickup);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onLevelTick);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onTagsUpdated);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerReloadListeners);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerCommands);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerNewRegistries);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerCapabilities);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			PluginManager.registerCircuits();
			registerComposts();
		});
	}

	private static void registerComposts() {
		// cast avoids stupid typos (IItemLike can be different than Item, then composter will not work)
		@SuppressWarnings({"unchecked", "rawtypes"})
		Object2FloatMap<Item> composts = ((Object2FloatMap) ComposterBlock.COMPOSTABLES);

		for (FeatureItem<?> fruit : CoreItems.FRUITS.getFeatures()) {
			composts.put(fruit.item(), 0.65f);
		}
		composts.put(CoreItems.MOULDY_WHEAT.item(), 0.65f);
		composts.put(CoreItems.DECAYING_WHEAT.item(), 0.65f);
		composts.put(CoreItems.MULCH.item(), 0.65f);
		composts.put(CoreItems.ASH.item(), 0.65f);
		composts.put(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), 0.65f);
		composts.put(CoreItems.PEAT.item(), 0.75f);
		composts.put(CoreItems.COMPOST.item(), 1f);
		for (Item pollen : ApicultureItems.POLLEN_CLUSTER.getItems()) {
			composts.put(pollen, 0.3f);
		}
		composts.put(ArboricultureItems.SAPLING.item(), 0.3f);
		composts.put(ArboricultureItems.POLLEN_FERTILE.item(), 0.3f);
		for (BlockItem leaves : ArboricultureBlocks.LEAVES_DECORATIVE.getItems()) {
			composts.put(leaves, 0.3f);
		}
		composts.put(LepidopterologyItems.COCOON_GE.item(), 0.3f);
	}

	private static void postItemRegistry() {
		PluginManager.registerGenetics();
		PluginManager.registerFarming();
		PluginManager.registerPollen();
	}

	// todo backpack
	/*private static void onItemPickup(EntityItemPickupEvent event) {
		if (event.isCanceled() || event.getResult() == Event.Result.ALLOW) {
			return;
		}
		PickupHandlerCore.onItemPickup(event.getEntity(), event.getItem());
	}*/

	private static void onLevelTick(ServerTickEvent.Post event) {
		TileStreamUpdateTracker.syncVisualUpdates(event.getServer());
	}

	private static void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.shouldUpdateStaticData()) {
			event.getRegistryAccess().registry(Registries.BIOME).ifPresent(registry -> ((ForestryClimateManager) IForestryApi.INSTANCE.getClimateManager()).onBiomesReloaded(registry));
		}
	}

	private static void registerReloadListeners(AddReloadListenerEvent event) {
		event.addListener((prepBarrier, resourceManager, prepProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			return prepBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
				RecipeManagers.invalidateCaches();
				PacketDistributor.sendToAllPlayers(new RecipeCachePacket());
			});
		});
	}

	private static void registerCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> forestryCommand = LiteralArgumentBuilder.literal("forestry");

		forestryCommand.then(DiagnosticsCommand.register());
		forestryCommand.then(DumpCommand.register());

		for (IForestryModule module : IForestryApi.INSTANCE.getModuleManager().getModulesForMod(ForestryConstants.MOD_ID)) {
			if (module instanceof BlankForestryModule forestryModule) {
				forestryModule.addToRootCommand(forestryCommand);
			}
		}

		event.getDispatcher().register(forestryCommand);
	}

	private static void registerNewRegistries(NewRegistryEvent event) {
		event.register(ForestryRegistries.CIRCUIT);
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
	public void registerPackets(PayloadRegistrar registrar) {
		registrar.playToServer(PacketIdServer.GUI_SELECTION_REQUEST, StreamCodec.of(PacketGuiSelectRequest::encode, PacketGuiSelectRequest::decode), PacketGuiSelectRequest::handle);
		registrar.playToServer(PacketIdServer.PIPETTE_CLICK, StreamCodec.of(PacketPipetteClick::encode, PacketPipetteClick::decode), PacketPipetteClick::handle);
		registrar.playToServer(PacketIdServer.CHIPSET_CLICK, StreamCodec.of(PacketChipsetClick::encode, PacketChipsetClick::decode), PacketChipsetClick::handle);
		registrar.playToServer(PacketIdServer.SOLDERING_IRON_CLICK, StreamCodec.of(PacketSolderingIronClick::encode, PacketSolderingIronClick::decode), PacketSolderingIronClick::handle);

		registrar.playToClient(PacketIdClient.ERROR_UPDATE, StreamCodec.of(PacketErrorUpdate::encode, PacketErrorUpdate::decode), PacketErrorUpdate::handle);
		registrar.playToClient(PacketIdClient.GUI_STREAM, StreamCodec.of(PacketGuiStream::encode, PacketGuiStream::decode), PacketGuiStream::handle);
		registrar.playToClient(PacketIdClient.GUI_LAYOUT_SELECT, StreamCodec.of(PacketGuiLayoutSelect::encode, PacketGuiLayoutSelect::decode), PacketGuiLayoutSelect::handle);
		registrar.playToClient(PacketIdClient.GUI_ENERGY, StreamCodec.of(PacketGuiEnergy::encode, PacketGuiEnergy::decode), PacketGuiEnergy::handle);
		registrar.playToClient(PacketIdClient.SOCKET_UPDATE, StreamCodec.of(PacketSocketUpdate::encode, PacketSocketUpdate::decode), PacketSocketUpdate::handle);
		registrar.playToClient(PacketIdClient.TILE_STREAM, StreamCodec.of(PacketTileStream::encode, PacketTileStream::decode), PacketTileStream::handle);
		registrar.playToClient(PacketIdClient.ACTIVE_UPDATE, StreamCodec.of(PacketActiveUpdate::encode, PacketActiveUpdate::decode), PacketActiveUpdate::handle);
		registrar.playToClient(PacketIdClient.ITEMSTACK_DISPLAY, StreamCodec.of(PacketItemStackDisplay::encode, PacketItemStackDisplay::decode), PacketItemStackDisplay::handle);
		registrar.playToClient(PacketIdClient.TANK_LEVEL_UPDATE, StreamCodec.of(PacketTankLevelUpdate::encode, PacketTankLevelUpdate::decode), PacketTankLevelUpdate::handle);
		registrar.playToClient(PacketIdClient.GENOME_TRACKER_UPDATE, StreamCodec.of(PacketGenomeTrackerSync::encode, PacketGenomeTrackerSync::decode), PacketGenomeTrackerSync::handle);
		registrar.playToClient(PacketIdClient.RECIPE_CACHE, StreamCodec.unit(new RecipeCachePacket()), RecipeCachePacket::handle);
		registrar.playToClient(PacketIdClient.REFRACTORY_WAX_ON, StreamCodec.of(PacketRefractoryWax::encode, PacketRefractoryWax::decode), PacketRefractoryWax::handle);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, v) -> new FluidHandlerItemStack(CoreDataComponents.FLUID_CONTENTS, stack, FluidType.BUCKET_VOLUME), CoreItems.PIPETTE);

		for (EnumContainerType type : EnumContainerType.values()) {
			event.registerItem(Capabilities.FluidHandler.ITEM, (stack, v) -> new FluidHandlerItemForestry(stack, type));
		}

		event.registerItem(ForestryCapabilities.SPECTACLE_VISION, (stack, v) -> ItemSpectacles.VISION, CoreItems.SPECTACLES);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new CoreClientHandler());
	}
}
