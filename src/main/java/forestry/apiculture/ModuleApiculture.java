package forestry.apiculture;

import forestry.apiculture.bees.genetics.BeeEffectManager;
import forestry.apiculture.bees.genetics.BeeSpeciesManager;
import forestry.apiculture.network.packets.BeeEffectSyncPacket;
import forestry.apiculture.network.packets.BeeSpeciesSyncPacket;
import forestry.core.platform.util.NetworkUtil;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import forestry.apiculture.network.ApiculturePacketIds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.ForestryCapabilities;
import forestry.api.client.IClientModuleHandler;
import forestry.api.core.ForestryEvent;
import forestry.api.core.TemperatureType;
import forestry.api.core.genetics.ForestryTaxa;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.apiculture.commands.CommandBee;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.bees.EnumPollenCluster;
import forestry.apiculture.apiarist.ItemArmorApiarist;
import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.network.packets.PacketHabitatBiomePointer;
import forestry.apiculture.proxy.ApicultureClientHandler;
import forestry.apiculture.apiarist.villagers.ApicultureVillagers;
import forestry.core.platform.util.SpeciesUtil;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import forestry.apiculture.tab.ApicultureCreativeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.plugin.IClientRegistration;
import forestry.api.core.genetics.ILifeStage;
import forestry.apiculture.client.BeeClientManager;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.apiimpl.client.plugin.ClientRegistration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import forestry.apiculture.apiarist.ArmorApiaristHelper;
import forestry.api.ForestryDataMaps;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@ForestryModule
public class ModuleApiculture extends BlankForestryModule {
	public static int ticksPerBeeWorkCycle = 550;
	public static boolean hivesDamageOnPeaceful = false;
	public static boolean hivesDamageUnderwater = true;
	public static boolean hivesDamageOnlyPlayers = false;
	public static boolean hiveDamageOnAttack = true;
	public static int maxFlowersSpawnedPerHive = 20;

	private static void onCommonSetup(FMLCommonSetupEvent event) {
	}

	private static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
		// BREWING RECIPES
		event.getBuilder().addRecipe(
			Ingredient.of(PotionContents.createItemStack(Items.POTION, Potions.AWKWARD)),
			Ingredient.of(ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.NORMAL, 1)),
			PotionContents.createItemStack(Items.POTION, Potions.HEALING));
		event.getBuilder().addRecipe(
			Ingredient.of(PotionContents.createItemStack(Items.POTION, Potions.AWKWARD)),
			Ingredient.of(ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.CRYSTALLINE, 1)),
			PotionContents.createItemStack(Items.POTION, Potions.REGENERATION));
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(ForestryCapabilities.BEE_PROTECTION, (stack, context) -> ItemArmorApiarist.BeeProtection.INSTANCE,
			ApicultureItems.APIARIST_HELMET.item(),
			ApicultureItems.APIARIST_CHEST.item(),
			ApicultureItems.APIARIST_LEGS.item(),
			ApicultureItems.APIARIST_BOOTS.item());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_PLAIN.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_SIEVE.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_SWARMER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_HYGROREGULATOR.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_STABILISER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_FAN.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ApicultureTiles.ALVEARY_HEATER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ApicultureTiles.ALVEARY_FAN.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ApicultureTiles.ALVEARY_HEATER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ApicultureTiles.ALVEARY_HYGROREGULATOR.tileType(), (tile, side) -> tile.getFluidHandler(side));
	}

	private static void onNetherBeeMate(ForestryEvent.BeeMatingEvent event) {
		if (event.getPrincess().getSpecies().getGenusName().equals(ForestryTaxa.GENUS_EMBITTERED) && event.getHousing().temperature() != TemperatureType.HELLISH) {
			event.setPrincess(SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.ZOMBIFIED).createIndividual());
		}
	}

	private static void modifySnifferLoot(LootTableLoadEvent event) {
		if (event.getKey().equals(BuiltInLootTables.SNIFFER_DIGGING)) {
			LootPool main = event.getTable().getPool("main");

			if (main != null) {
				List<LootPoolEntryContainer> entries = new ArrayList<>(main.entries);
				entries.add(LootItem.lootTableItem(ApicultureItems.AMBER_DRONE).build());
				main.entries = entries;
			}
		}
	}

	// todo config
	public static double getSecondPrincessChance() {
		return (float) 0;
	}

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.APICULTURE;
	}

	// declared so the load order that carries the reload ordering is stated rather than incidental
	@Override
	public List<ResourceLocation> getModuleDependencies() {
		return List.of(ForestryModuleIds.CORE);
	}

	/**
	 * Order within this method matters. Apply order follows registration order, and bee species
	 * projection resolves each genome's flower type and bee_effect reference as it runs, so both must
	 * already be loaded. Core's taxa are registered before any module for the same reason.
	 */
	@Override
	public void registerReloadListeners(AddReloadListenerEvent event) {
		// Flower types load in ModuleCore, not here: butterflies carry the same FLOWER_TYPE
		// chromosome, so base owns them.

		// Load bee effects from the "bee_effect" folder. Registered before BeeSpeciesManager: species
		// projection resolves each genome's bee_effect reference via getBeeEffect, so effects must exist
		// first.
		event.addListener(BeeEffectManager.INSTANCE);

		// Load bee species from the "bee_species" datapack folder and rebuild the live species map from
		// them. SimpleJsonResourceReloadListener#apply already runs on the game executor, so no extra
		// marshalling is needed here. Core registers the mutation rebuild after every module, and
		// mutations must resolve species that already exist in the live map.
		event.addListener(BeeSpeciesManager.INSTANCE);
	}

	/**
	 * Flower types and effects are sent before species for the same reason they load first: the client
	 * rebuilds its species index from the species packet's handler, and projection reads both.
	 */
	@Override
	public void syncDatapack(OnDatapackSyncEvent event) {
		BeeEffectSyncPacket beeEffectPacket = new BeeEffectSyncPacket(BeeEffectManager.INSTANCE.getEffects());
		BeeSpeciesSyncPacket beePacket = new BeeSpeciesSyncPacket(BeeSpeciesManager.INSTANCE.getDefinitions());
		event.getRelevantPlayers().forEach(player -> {
			NetworkUtil.sendToPlayer(beeEffectPacket, player);
			NetworkUtil.sendToPlayer(beePacket, player);
		});
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ApicultureCreativeTab::addToForestryTab);
		modBus.addListener(ModuleApiculture::registerCapabilities);
		modBus.addListener(ModuleApiculture::registerDataMaps);
		modBus.addListener(ModuleApiculture::onCommonSetup);

		NeoForge.EVENT_BUS.addListener(ModuleApiculture::registerBrewingRecipes);
		NeoForge.EVENT_BUS.addListener(ApicultureVillagers::villagerTrades);
		NeoForge.EVENT_BUS.addListener(ModuleApiculture::onNetherBeeMate);
		NeoForge.EVENT_BUS.addListener(ModuleApiculture::modifySnifferLoot);
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.SWARMER_FEED);
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandBee.register());
	}

	@Override
	public void setupApi() {
		BeeManager.armorApiaristHelper = new ArmorApiaristHelper();
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.clientbound(ApiculturePacketIds.BEE_EFFECT_SYNC, BeeEffectSyncPacket::encode, BeeEffectSyncPacket::decode, BeeEffectSyncPacket::handle);
		registry.clientbound(ApiculturePacketIds.BEE_SPECIES_SYNC, BeeSpeciesSyncPacket::encode, BeeSpeciesSyncPacket::decode, BeeSpeciesSyncPacket::handle);
		registry.clientbound(ApiculturePacketIds.BEE_LOGIC_ACTIVE, PacketBeeLogicActive::encode, PacketBeeLogicActive::decode, PacketBeeLogicActive::handle);
		registry.clientbound(ApiculturePacketIds.HABITAT_BIOME_POINTER, PacketHabitatBiomePointer::encode, PacketHabitatBiomePointer::decode, PacketHabitatBiomePointer::handle);
		registry.clientbound(ApiculturePacketIds.ALVEARY_CONTROLLER_CHANGE, PacketAlvearyChange::encode, PacketAlvearyChange::decode, PacketAlvearyChange::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ApicultureClientHandler());
	}

	@Override
	public void installClientManagers(IClientRegistration registration) {
		ClientRegistration impl = (ClientRegistration) registration;

		// id-keyed: resolving a specific species happens at render time, so the (possibly
		// datapack-driven) species list is not needed here.
		IdentityHashMap<ILifeStage, ResourceLocation> defaultBeeModels = new IdentityHashMap<>();
		IdentityHashMap<ILifeStage, Map<ResourceLocation, ResourceLocation>> customBeeModels = new IdentityHashMap<>();

		for (ILifeStage stage : SpeciesUtil.BEE_TYPE.get().getLifeStages()) {
			ResourceLocation defaultModel = Objects.requireNonNull(impl.getDefaultBeeModel(stage), "IClientRegistration.setDefaultBeeModel has not been called for life stage " + stage.getSerializedName() + ", unable to resolve bee default model");
			defaultBeeModels.put(stage, defaultModel);
			customBeeModels.put(stage, impl.getBeeModels().getOrDefault(stage, Map.of()));
		}
		((ForestryClientApiImpl) IForestryClientApi.INSTANCE).setBeeManager(new BeeClientManager(defaultBeeModels, customBeeModels));
	}
}
