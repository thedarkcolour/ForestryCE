package forestry.apiculture;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.client.IClientModuleHandler;
import forestry.api.core.TemperatureType;
import forestry.api.event.BeeMatingEvent;
import forestry.api.genetics.ForestryTaxa;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.commands.CommandBee;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.ItemArmorApiarist;
import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.proxy.ApicultureClientHandler;
import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.core.data.LootTableHelper;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

@ForestryModule
public class ModuleApiculture extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.APICULTURE;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleApiculture::registerCapabilities);
		modBus.addListener(ModuleApiculture::registerBrewingRecipes);

		NeoForge.EVENT_BUS.addListener(ApicultureVillagers::villagerTrades);
		NeoForge.EVENT_BUS.addListener(ModuleApiculture::onNetherBeeMate);
		NeoForge.EVENT_BUS.addListener(ModuleApiculture::modifySnifferLoot);
	}

	private static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
		// BREWING RECIPES
		PotionBrewing.Builder builder = event.getBuilder();

		builder.addMix(Potions.AWKWARD, ApicultureItems.POLLEN_CLUSTER.item(EnumPollenCluster.NORMAL), Potions.HEALING);
		builder.addMix(Potions.AWKWARD, ApicultureItems.POLLEN_CLUSTER.item(EnumPollenCluster.CRYSTALLINE), Potions.REGENERATION);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(ForestryCapabilities.BEE_PROTECTION, (stack, v) -> ItemArmorApiarist.ArmorApiarist.INSTANCE, ApicultureItems.APIARIST_HELMET, ApicultureItems.APIARIST_CHEST, ApicultureItems.APIARIST_LEGS, ApicultureItems.APIARIST_BOOTS);
	}

	private static void onNetherBeeMate(BeeMatingEvent event) {
		if (event.getPrincess().getSpecies().getGenusName().equals(ForestryTaxa.GENUS_EMBITTERED) && event.getHousing().temperature() != TemperatureType.HELLISH) {
			event.setPrincess(SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.ZOMBIFIED).createIndividual());
		}
	}

	private static void modifySnifferLoot(LootTableLoadEvent event) {
		if (event.getName().equals(BuiltInLootTables.SNIFFER_DIGGING.location())) {
			LootPool main = event.getTable().getPool("main");

			if (main != null) {
				ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builderWithExpectedSize(main.entries.size() + 1);
				entries.addAll(main.entries);
				entries.add(LootTableHelper.beeLoot(ForestryBeeSpecies.RELIC).build());
				main.entries = entries.build();
			}
		}
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandBee.register());
	}

	@Override
	public void registerPackets(PayloadRegistrar registrar) {
		registrar.playToClient(PacketIdClient.BEE_LOGIC_ACTIVE, StreamCodec.of(PacketBeeLogicActive::encode, PacketBeeLogicActive::decode), PacketBeeLogicActive::handle);
		registrar.playToClient(PacketIdClient.ALVEARY_CONTROLLER_CHANGE, StreamCodec.of(PacketAlvearyChange::encode, PacketAlvearyChange::decode), PacketAlvearyChange::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ApicultureClientHandler());
	}
}
