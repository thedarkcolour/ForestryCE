package forestry.arboriculture;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import forestry.arboriculture.loot.GrafterLootModifier;
import forestry.api.ForestryConstants;
import forestry.arboriculture.trees.genetics.TreeSpeciesManager;
import forestry.arboriculture.network.TreeSpeciesSyncPacket;
import forestry.core.platform.util.NetworkUtil;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import forestry.arboriculture.network.ArboriculturePacketIds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.arboriculture.client.ArboricultureClientHandler;
import forestry.arboriculture.commands.TreeCommand;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.wood.ForestryBoatDispenserBehavior;
import forestry.arboriculture.network.PacketRipeningUpdate;
import forestry.arboriculture.villagers.ArboricultureVillagers;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import forestry.arboriculture.tab.ArboricultureCreativeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import com.mojang.datafixers.util.Pair;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;
import forestry.api.client.plugin.IClientRegistration;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.apiimpl.client.plugin.ClientRegistration;
import forestry.arboriculture.client.TreeClientManager;
import java.util.HashMap;
import java.util.Map;
import forestry.arboriculture.wood.ForestryWoodType;

@ForestryModule
public class ModuleArboriculture extends BlankForestryModule {
	// whether a tree can pollinate itself; read by TreeUtil.canPollinate
	public static boolean doSelfPollination = false;

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.ARBORICULTURE;
	}

	// declared so the load order that carries the reload ordering is stated rather than incidental
	@Override
	public List<ResourceLocation> getModuleDependencies() {
		return List.of(ForestryModuleIds.CORE);
	}

	@Override
	public void registerReloadListeners(AddReloadListenerEvent event) {
		// Load tree species from the "tree_species" datapack folder and rebuild the live species map from
		// them. Core registers the mutation rebuild after every module, and mutations must resolve
		// species that already exist in the live map.
		event.addListener(TreeSpeciesManager.INSTANCE);
	}

	@Override
	public void syncDatapack(OnDatapackSyncEvent event) {
		TreeSpeciesSyncPacket treePacket = new TreeSpeciesSyncPacket(TreeSpeciesManager.INSTANCE.getDefinitions());
		event.getRelevantPlayers().forEach(player -> NetworkUtil.sendToPlayer(treePacket, player));
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleArboriculture::registerGlobalLootModifiers);
		modBus.addListener(ArboricultureCreativeTab::addToForestryTab);
		NeoForge.EVENT_BUS.addListener(ArboricultureVillagers::villagerTrades);

		modBus.addListener(ModuleArboriculture::registerCapabilities);
		modBus.addListener(ModuleArboriculture::commonSetup);
		modBus.addListener(ModuleArboriculture::registerHangingSignBlocks);
		NeoForge.EVENT_BUS.addListener(ModuleArboriculture::modifySnifferLoot);
	}

	/**
	 * Adds Forestry's ceiling and wall hanging sign blocks to vanilla's
	 * {@link BlockEntityType#HANGING_SIGN} valid-blocks set. This is required because
	 * {@code HangingSignBlockEntity}'s public constructor hardcodes that vanilla type, so
	 * any Forestry-owned BlockEntityType for hanging signs would never be used at runtime
	 * (breaking save validation and tickers).
	 */
	private static void registerHangingSignBlocks(BlockEntityTypeAddBlocksEvent event) {
		Block[] hangingSignBlocks = Stream.concat(
			ArboricultureBlocks.HANGING_SIGN.getList().stream(),
			ArboricultureBlocks.WALL_HANGING_SIGN.getList().stream()
		).toArray(Block[]::new);
		event.modify(BlockEntityType.HANGING_SIGN, hangingSignBlocks);
	}

	private static void modifySnifferLoot(LootTableLoadEvent event) {
		if (event.getKey().equals(BuiltInLootTables.SNIFFER_DIGGING)) {
			LootPool main = event.getTable().getPool("main");

			if (main != null) {
				List<LootPoolEntryContainer> entries = new ArrayList<>(main.entries);
				entries.add(LootItem.lootTableItem(ArboricultureItems.AMBER_SAPLING_FOSSIL).build());
				main.entries = entries;
			}
		}
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(TreeCommand.register());
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(Capabilities.ItemHandler.ENTITY, ArboricultureEntities.CHEST_BOAT.entityType(), (boat, context) -> boat.isAlive() ? new InvWrapper(boat) : null);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			for (ForestryWoodType type : ForestryWoodType.VALUES) {
				DispenserBlock.registerBehavior(ArboricultureItems.BOAT.item(type), new ForestryBoatDispenserBehavior(type, false));
				DispenserBlock.registerBehavior(ArboricultureItems.CHEST_BOAT.item(type), new ForestryBoatDispenserBehavior(type, true));
				WoodType.register(type.getWoodType());
			}
		});
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.clientbound(ArboriculturePacketIds.TREE_SPECIES_SYNC, TreeSpeciesSyncPacket::encode, TreeSpeciesSyncPacket::decode, TreeSpeciesSyncPacket::handle);
		registry.clientbound(ArboriculturePacketIds.RIPENING_UPDATE, PacketRipeningUpdate::encode, PacketRipeningUpdate::decode, PacketRipeningUpdate::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ArboricultureClientHandler());
	}

	// same registry name generated loot modifier JSON already references
	private static void registerGlobalLootModifiers(RegisterEvent event) {
		event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper ->
			helper.register(ForestryConstants.forestry("grafter_modifier"), GrafterLootModifier.CODEC));
	}

	@Override
	public void applyClientPluginRegistration(IClientRegistration registration) {
		ClientRegistration impl = (ClientRegistration) registration;

		// id-keyed: resolving a species happens at render time by id, so the (datapack-driven) species list is not
		// needed to build the sprite/model maps below.
		HashMap<ResourceLocation, ILeafSprite> spritesById = impl.getLeafSprites();
		HashMap<ResourceLocation, ILeafTint> tintsById = impl.getTints();
		HashMap<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> modelsById = impl.getSaplingModels();

		// The escritoire-color tint fallback (for the ~40 built-in species that register no explicit client tint) is
		// applied lazily at render time in TreeClientManager#getTint from the species object itself, so no species-list
		// iteration is needed here and datapack-added species get the same fallback reloadably.

		// For any species id that has a leaf sprite but no explicit sapling model, synthesize the default-path pair
		// (removing the "tree_" prefix), exactly as the old per-species loop did.
		Map<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> models = new HashMap<>(modelsById);
		for (ResourceLocation id : spritesById.keySet()) {
			models.computeIfAbsent(id, sid -> {
				String path = sid.getPath().replace("tree_", "");
				return Pair.of(
					ResourceLocation.fromNamespaceAndPath(sid.getNamespace(), "block/" + path + "_sapling"),
					ResourceLocation.fromNamespaceAndPath(sid.getNamespace(), "item/" + path + "_sapling")
				);
			});
		}

		((ForestryClientApiImpl) IForestryClientApi.INSTANCE).setTreeManager(new TreeClientManager(
			new HashMap<>(spritesById), new HashMap<>(tintsById), models
		));
	}
}
