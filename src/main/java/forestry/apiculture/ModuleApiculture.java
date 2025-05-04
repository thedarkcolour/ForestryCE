package forestry.apiculture;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.IArmorApiarist;
import forestry.api.client.IClientModuleHandler;
import forestry.api.core.ForestryEvent;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ForestryTaxa;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.apiculture.commands.CommandBee;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.network.packets.PacketHabitatBiomePointer;
import forestry.apiculture.proxy.ApicultureClientHandler;
import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Consumer;

@ForestryModule
public class ModuleApiculture extends BlankForestryModule {
	public static int ticksPerBeeWorkCycle = 550;
	public static boolean hivesDamageOnPeaceful = false;
	public static boolean hivesDamageUnderwater = true;
	public static boolean hivesDamageOnlyPlayers = false;
	public static boolean hiveDamageOnAttack = true;
	public static boolean doSelfPollination = false;
	public static int maxFlowersSpawnedPerHive = 20;

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		// BREWING RECIPES
		BrewingRecipeRegistry.addRecipe(
			Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
			Ingredient.of(ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.NORMAL, 1)),
			PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING));
		BrewingRecipeRegistry.addRecipe(
			Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
			Ingredient.of(ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.CRYSTALLINE, 1)),
			PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION));
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IArmorApiarist.class);
	}

	private static void onNetherBeeMate(ForestryEvent.BeeMatingEvent event) {
		if (event.getPrincess().getSpecies().getGenusName().equals(ForestryTaxa.GENUS_EMBITTERED) && event.getHousing().temperature() != TemperatureType.HELLISH) {
			event.setPrincess(SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.ZOMBIFIED).createIndividual());
		}
	}

	private static void modifySnifferLoot(LootTableLoadEvent event) {
		if (event.getName().equals(BuiltInLootTables.SNIFFER_DIGGING)) {
			LootPool main = event.getTable().getPool("main");

			if (main != null) {
				LootPoolEntryContainer[] entries = new LootPoolEntryContainer[main.entries.length + 1];
				System.arraycopy(main.entries, 0, entries, 0, main.entries.length);
				entries[main.entries.length] = LootItem.lootTableItem(ApicultureItems.AMBER_DRONE).build();
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

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleApiculture::registerCapabilities);
		modBus.addListener(ModuleApiculture::onCommonSetup);

		MinecraftForge.EVENT_BUS.addListener(ApicultureVillagers::villagerTrades);
		MinecraftForge.EVENT_BUS.addListener(ModuleApiculture::onNetherBeeMate);
		MinecraftForge.EVENT_BUS.addListener(ModuleApiculture::modifySnifferLoot);
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
		registry.clientbound(PacketIdClient.BEE_LOGIC_ACTIVE, PacketBeeLogicActive.class, PacketBeeLogicActive::decode, PacketBeeLogicActive::handle);
		registry.clientbound(PacketIdClient.HABITAT_BIOME_POINTER, PacketHabitatBiomePointer.class, PacketHabitatBiomePointer::decode, PacketHabitatBiomePointer::handle);
		registry.clientbound(PacketIdClient.ALVERAY_CONTROLLER_CHANGE, PacketAlvearyChange.class, PacketAlvearyChange::decode, PacketAlvearyChange::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ApicultureClientHandler());
	}
}
