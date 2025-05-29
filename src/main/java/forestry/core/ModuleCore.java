package forestry.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IForestryModule;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiimpl.plugin.PluginManager;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.loot.GrafterLootModifier;
import forestry.core.blocks.TileStreamUpdateTracker;
import forestry.core.client.CoreClientHandler;
import forestry.core.climate.ForestryClimateManager;
import forestry.core.commands.DiagnosticsCommand;
import forestry.core.commands.DumpCommand;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.loot.ConditionLootModifier;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.core.network.packets.*;
import forestry.core.owner.GameProfileDataSerializer;
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
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

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
		modBus.addListener(ModuleCore::registerGlobalLootModifiers);
		ModUtil.addRegistryListener(Registries.ITEM, ModuleCore::postItemRegistry);

		ModuleUtil.loadFeatureProviders();
		NeoForge.EVENT_BUS.addListener(ModuleCore::onItemPickup);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onLevelTick);
		NeoForge.EVENT_BUS.addListener(ModuleCore::onTagsUpdated);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerReloadListeners);
		NeoForge.EVENT_BUS.addListener(ModuleCore::registerCommands);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			PluginManager.registerCircuits();
			EntityDataSerializers.registerSerializer(GameProfileDataSerializer.INSTANCE);
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

	private static void registerGlobalLootModifiers(RegisterEvent event) {
		event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> {
			helper.register(ForestryConstants.forestry("condition_modifier"), ConditionLootModifier.CODEC);
			helper.register(ForestryConstants.forestry("grafter_modifier"), GrafterLootModifier.CODEC);
		});
	}

	private static void postItemRegistry() {
		PluginManager.registerGenetics();
		PluginManager.registerFarming();
		PluginManager.registerPollen();
	}

	private static void onItemPickup(EntityItemPickupEvent event) {
		if (event.isCanceled() || event.getResult() == Event.Result.ALLOW) {
			return;
		}
		PickupHandlerCore.onItemPickup(event.getEntity(), event.getItem());
	}

	private static void onLevelTick(LevelTickEvent.Post event) {
		TileStreamUpdateTracker.syncVisualUpdates();
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
		registrar.playToClient(PacketIdClient.GUI_UPDATE, StreamCodec.of(PacketGuiStream::encode, PacketGuiStream::decode), PacketGuiStream::handle);
		registrar.playToClient(PacketIdClient.GUI_LAYOUT_SELECT, StreamCodec.of(PacketGuiLayoutSelect::encode, PacketGuiLayoutSelect::decode), PacketGuiLayoutSelect::handle);
		registrar.playToClient(PacketIdClient.GUI_ENERGY, StreamCodec.of(PacketGuiEnergy::encode, PacketGuiEnergy::decode), PacketGuiEnergy::handle);
		registrar.playToClient(PacketIdClient.SOCKET_UPDATE, StreamCodec.of(PacketSocketUpdate::encode, PacketSocketUpdate::decode), PacketSocketUpdate::handle);
		registrar.playToClient(PacketIdClient.TILE_FORESTRY_UPDATE, StreamCodec.of(PacketTileStream::encode, PacketTileStream::decode), PacketTileStream::handle);
		registrar.playToClient(PacketIdClient.TILE_FORESTRY_ACTIVE, StreamCodec.of(PacketActiveUpdate::encode, PacketActiveUpdate::decode), PacketActiveUpdate::handle);
		registrar.playToClient(PacketIdClient.ITEMSTACK_DISPLAY, StreamCodec.of(PacketItemStackDisplay::encode, PacketItemStackDisplay::decode), PacketItemStackDisplay::handle);
		registrar.playToClient(PacketIdClient.GENOME_TRACKER_UPDATE, StreamCodec.of(PacketTankLevelUpdate::encode, PacketTankLevelUpdate::decode), PacketTankLevelUpdate::handle);
		registrar.playToClient(PacketIdClient.TANK_LEVEL_UPDATE, StreamCodec.of(PacketGenomeTrackerSync::encode, PacketGenomeTrackerSync::decode), PacketGenomeTrackerSync::handle);
		registrar.playToClient(PacketIdClient.RECIPE_CACHE, StreamCodec.of(RecipeCachePacket::encode, RecipeCachePacket::decode), RecipeCachePacket::handle);
		registrar.playToClient(PacketIdClient.REFRACTORY_WAX_ON, StreamCodec.of(PacketRefractoryWax::encode, PacketRefractoryWax::decode), PacketRefractoryWax::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new CoreClientHandler());
	}
}
