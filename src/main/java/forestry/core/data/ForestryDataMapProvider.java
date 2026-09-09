package forestry.core.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

import forestry.api.ForestryConstants;
import forestry.api.ForestryDataMaps;
import forestry.api.core.machines.fuels.BiogasEngineFuel;
import forestry.api.core.machines.fuels.FermenterFuel;
import forestry.api.core.machines.fuels.MoistenerFuel;
import forestry.api.core.machines.fuels.PeatEngineFuel;
import forestry.api.core.machines.fuels.RainmakerSubstrate;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.bees.PollenClusterItem;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.content.resources.EnumCraftingMaterial;

/**
 * Generates the NeoForge built-in data maps for Forestry items and villagers:
 * <ul>
 *     <li>{@code neoforge:compostables} - composter chances (replaces the old imperative
 *     {@code ComposterBlock.COMPOSTABLES} mutation)</li>
 *     <li>{@code neoforge:furnace_fuels} - burn times for peat, charcoal, the log piles, plywood and cork
 *     (replacing the old per-item {@code ItemProperties.burnTime}), so they work in vanilla furnaces, Create
 *     blaze burners, etc.</li>
 *     <li>{@code neoforge:raid_hero_gifts} - Hero of the Village gifts for Forestry professions.</li>
 * </ul>
 */
public class ForestryDataMapProvider extends DataMapProvider {
	public ForestryDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		gatherCompostables();
		gatherFurnaceFuels();
		gatherRaidHeroGifts();
		gatherFermenterFuels();
		gatherMoistenerFuels();
		gatherRainmakerFuels();
		gatherEngineFuels();
		gatherSwarmerFeed();
	}

	private void gatherFermenterFuels() {
		Builder<FermenterFuel, Item> fuels = builder(ForestryDataMaps.FERMENTER_FUELS);

		fuels.add(CoreItems.FERTILIZER_COMPOUND.item().builtInRegistryHolder(), new FermenterFuel(56, 200), false);
		fuels.add(CoreItems.COMPOST.item().builtInRegistryHolder(), new FermenterFuel(48, 250), false);
		fuels.add(CoreItems.MULCH.item().builtInRegistryHolder(), new FermenterFuel(48, 250), false);
	}

	private void gatherMoistenerFuels() {
		Builder<MoistenerFuel, Item> fuels = builder(ForestryDataMaps.MOISTENER_FUELS);

		// Each entry names the item the working slot leaves behind, so the three form a chain
		fuels.add(Items.WHEAT.builtInRegistryHolder(), new MoistenerFuel(CoreItems.MOULDY_WHEAT.stack(), 0, 300), false);
		fuels.add(CoreItems.MOULDY_WHEAT.item().builtInRegistryHolder(), new MoistenerFuel(CoreItems.DECAYING_WHEAT.stack(), 1, 600), false);
		fuels.add(CoreItems.DECAYING_WHEAT.item().builtInRegistryHolder(), new MoistenerFuel(CoreItems.MULCH.stack(), 2, 900), false);
	}

	private void gatherRainmakerFuels() {
		Builder<RainmakerSubstrate, Item> fuels = builder(ForestryDataMaps.RAINMAKER_FUELS);

		fuels.add(CoreItems.IODINE_CHARGE.item().builtInRegistryHolder(), new RainmakerSubstrate(10000, 0.01f, false), false);
		fuels.add(CoreItems.DISSIPATION_CHARGE.item().builtInRegistryHolder(), new RainmakerSubstrate(0, 0.075f, true), false);
	}

	private void gatherEngineFuels() {
		Builder<BiogasEngineFuel, Fluid> biogas = builder(ForestryDataMaps.BIOGAS_FUELS);

		biogas.add(ForestryFluids.BIOMASS.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_BIOMASS, Constants.ENGINE_CYCLE_DURATION_BIOMASS, 1), false);
		biogas.add(NeoForgeMod.MILK, new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_MILK, Constants.ENGINE_CYCLE_DURATION_MILK, 3), false);
		biogas.add(ForestryFluids.SEED_OIL.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_SEED_OIL, Constants.ENGINE_CYCLE_DURATION_SEED_OIL, 1), false);
		biogas.add(ForestryFluids.HONEY.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_HONEY, Constants.ENGINE_CYCLE_DURATION_HONEY, 1), false);
		biogas.add(ForestryFluids.JUICE.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_JUICE, Constants.ENGINE_CYCLE_DURATION_JUICE, 2), false);

		Builder<BiogasEngineFuel, Fluid> combustion = builder(ForestryDataMaps.COMBUSTION_FUELS);

		combustion.add(ForestryFluids.BIO_ETHANOL.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_ETHANOL, Constants.ENGINE_CYCLE_DURATION_ETHANOL, 1), false);

		// Power per cycle is unread for a coolant, so it stays 0
		Builder<BiogasEngineFuel, Fluid> coolants = builder(ForestryDataMaps.COMBUSTION_COOLANTS);

		coolants.add(Fluids.WATER.builtInRegistryHolder(), new BiogasEngineFuel(0, Constants.ENGINE_COOLANT_VALUE_WATER, 0), false);
		coolants.add(ForestryFluids.ICE.holder(), new BiogasEngineFuel(0, Constants.ENGINE_COOLANT_VALUE_CRUSHED_ICE, 20), false);

		Builder<PeatEngineFuel, Item> peat = builder(ForestryDataMaps.PEAT_FUELS);

		peat.add(CoreItems.PEAT.item().builtInRegistryHolder(), new PeatEngineFuel(Constants.ENGINE_COPPER_FUEL_VALUE_PEAT, Constants.ENGINE_COPPER_CYCLE_DURATION_PEAT), false);
		peat.add(CoreItems.BITUMINOUS_PEAT.item().builtInRegistryHolder(), new PeatEngineFuel(Constants.ENGINE_COPPER_FUEL_VALUE_BITUMINOUS_PEAT, Constants.ENGINE_COPPER_CYCLE_DURATION_BITUMINOUS_PEAT), false);
	}

	private void gatherSwarmerFeed() {
		Builder<Float, Item> feed = builder(ForestryDataMaps.SWARMER_FEED);

		feed.add(ApicultureItems.ROYAL_JELLY.item().builtInRegistryHolder(), 0.01f, false);
	}

	private void gatherCompostables() {
		Builder<Compostable, Item> composts = builder(NeoForgeDataMaps.COMPOSTABLES);

		for (Item fruit : CoreItems.FRUITS.getItems()) {
			compostable(composts, fruit, 0.65f);
		}
		compostable(composts, CoreItems.MOULDY_WHEAT.item(), 0.65f);
		compostable(composts, CoreItems.DECAYING_WHEAT.item(), 0.65f);
		compostable(composts, CoreItems.MULCH.item(), 0.65f);
		compostable(composts, CoreItems.ASH.item(), 0.65f);
		compostable(composts, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), 0.65f);
		compostable(composts, CoreItems.PEAT.item(), 0.75f);
		compostable(composts, CoreItems.COMPOST.item(), 1f);
		for (PollenClusterItem pollen : ApicultureItems.POLLEN_CLUSTER.getItems()) {
			compostable(composts, pollen, 0.3f);
		}
		compostable(composts, ArboricultureItems.TREE_SAPLING.item(), 0.3f);
		compostable(composts, ArboricultureItems.TREE_POLLEN.item(), 0.3f);
		for (BlockItem leaves : ArboricultureBlocks.LEAVES_DECORATIVE.getItems()) {
			compostable(composts, leaves, 0.3f);
		}
		// The cocoon entry is added by the butterflies jar, which merges into this data map from its own file
	}

	private void gatherRaidHeroGifts() {
		Builder<RaidHeroGift, VillagerProfession> gifts = builder(NeoForgeDataMaps.RAID_HERO_GIFTS);
		gifts.add(ForestryConstants.forestry("beekeeper"), new RaidHeroGift(ForestryGiftLootTables.BEEKEEPER_GIFT), false);
		gifts.add(ForestryConstants.forestry("arborist"), new RaidHeroGift(ForestryGiftLootTables.ARBORIST_GIFT), false);
	}

	private void gatherFurnaceFuels() {
		Builder<FurnaceFuel, Item> fuels = builder(NeoForgeDataMaps.FURNACE_FUELS);

		furnaceFuel(fuels, CharcoalBlocks.CHARCOAL.item(), 16000);
		furnaceFuel(fuels, CoreItems.BITUMINOUS_PEAT.item(), 4200);
		furnaceFuel(fuels, CoreItems.PEAT.item(), 2000);
		furnaceFuel(fuels, CharcoalBlocks.LOG_PILE.item(), 1200);
		furnaceFuel(fuels, CharcoalBlocks.DECORATIVE_LOG_PILE.item(), 1200);
		furnaceFuel(fuels, CoreBlocks.PLYWOOD_BLOCK.item(), 300);
		furnaceFuel(fuels, CoreBlocks.CORK.item(), 300);
		furnaceFuel(fuels, CoreBlocks.PLYWOOD_SHEET.item(), 50);
	}

	private static void compostable(Builder<Compostable, Item> builder, ItemLike item, float chance) {
		builder.add(BuiltInRegistries.ITEM.getKey(item.asItem()), new Compostable(chance), false);
	}

	private static void furnaceFuel(Builder<FurnaceFuel, Item> builder, ItemLike item, int burnTime) {
		builder.add(BuiltInRegistries.ITEM.getKey(item.asItem()), new FurnaceFuel(burnTime), false);
	}

	@Override
	public String getName() {
		return "Forestry Data Maps";
	}
}
