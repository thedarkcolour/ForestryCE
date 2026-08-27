package forestry.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import forestry.api.ForestryConstants;
import forestry.api.ForestryDataMaps;
import forestry.api.IForestryApi;
import forestry.api.core.machines.fuels.BiogasEngineFuel;
import forestry.api.core.machines.fuels.FermenterFuel;
import forestry.api.core.machines.fuels.MoistenerFuel;
import forestry.api.core.machines.fuels.PeatEngineFuel;
import forestry.api.core.machines.fuels.RainmakerFuel;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.features.CoreItems;
import forestry.core.platform.config.Constants;
import forestry.core.platform.config.Preference;
import forestry.core.platform.fluids.ForestryFluids;

/**
 * Guard for the fuel data maps that replaced the old {@code FuelManager} item maps. A machine reads
 * its fuel through the data map now, so a map that fails to register, or a generated file that fails
 * to load, leaves the machine refusing every fuel with nothing in the log to say why.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class FuelDataMapTest {
	@GameTest(template = "empty")
	public static void everyFuelDataMapLoaded(GameTestHelper helper) {
		List<String> empty = new ArrayList<>();

		checkItems(empty, "fermenter_fuels", ForestryDataMaps.FERMENTER_FUELS);
		checkItems(empty, "moistener_fuels", ForestryDataMaps.MOISTENER_FUELS);
		checkItems(empty, "rainmaker_fuels", ForestryDataMaps.RAINMAKER_FUELS);
		checkItems(empty, "peat_fuels", ForestryDataMaps.PEAT_FUELS);
		checkItems(empty, "swarmer_feed", ForestryDataMaps.SWARMER_FEED);
		checkItems(empty, "farm_fertilizers", ForestryDataMaps.FARM_FERTILIZERS);
		checkFluids(empty, "biogas_fuels", ForestryDataMaps.BIOGAS_FUELS);
		checkFluids(empty, "combustion_fuels", ForestryDataMaps.COMBUSTION_FUELS);
		checkFluids(empty, "combustion_coolants", ForestryDataMaps.COMBUSTION_COOLANTS);

		if (!empty.isEmpty()) {
			helper.fail(empty.size() + " fuel data map(s) resolved no entries: " + String.join(", ", empty));
			return;
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void fuelValuesSurvivedTheMoveOffFuelManager(GameTestHelper helper) {
		FermenterFuel fertilizer = data(CoreItems.FERTILIZER_COMPOUND.item(), ForestryDataMaps.FERMENTER_FUELS);
		helper.assertTrue(fertilizer != null, "Fertilizer is not a fermenter fuel");
		helper.assertTrue(fertilizer.fermentPerCycle() == Preference.FERMENTED_CYCLE_FERTILIZER, "Wrong ferment per cycle for fertilizer");
		helper.assertTrue(fertilizer.burnDuration() == Preference.FERMENTATION_DURATION_FERTILIZER, "Wrong burn duration for fertilizer");

		// The three moistener resources form a chain, so each product must itself be the next key
		MoistenerFuel wheat = data(Items.WHEAT, ForestryDataMaps.MOISTENER_FUELS);
		helper.assertTrue(wheat != null && wheat.stage() == 0, "Wheat is not moistener stage 0");
		helper.assertTrue(wheat.product().is(CoreItems.MOULDY_WHEAT.item()), "Wheat does not moisten into mouldy wheat");
		MoistenerFuel mouldy = data(CoreItems.MOULDY_WHEAT.item(), ForestryDataMaps.MOISTENER_FUELS);
		helper.assertTrue(mouldy != null && mouldy.product().is(CoreItems.DECAYING_WHEAT.item()), "Mouldy wheat does not moisten into decaying wheat");
		MoistenerFuel decaying = data(CoreItems.DECAYING_WHEAT.item(), ForestryDataMaps.MOISTENER_FUELS);
		helper.assertTrue(decaying != null && decaying.product().is(CoreItems.MULCH.item()), "Decaying wheat does not moisten into mulch");

		PeatEngineFuel peat = data(CoreItems.PEAT.item(), ForestryDataMaps.PEAT_FUELS);
		helper.assertTrue(peat != null, "Peat is not a peat engine fuel");
		helper.assertTrue(peat.powerPerCycle() == Constants.ENGINE_COPPER_FUEL_VALUE_PEAT, "Wrong power per cycle for peat");
		helper.assertTrue(peat.burnDuration() == Constants.ENGINE_COPPER_CYCLE_DURATION_PEAT, "Wrong burn duration for peat");

		BiogasEngineFuel biomass = ForestryFluids.BIOMASS.getFluid().builtInRegistryHolder().getData(ForestryDataMaps.BIOGAS_FUELS);
		helper.assertTrue(biomass != null, "Biomass is not a biogas engine fuel");
		helper.assertTrue(biomass.powerPerCycle() == Constants.ENGINE_FUEL_VALUE_BIOMASS, "Wrong power per cycle for biomass");

		BiogasEngineFuel water = Fluids.WATER.builtInRegistryHolder().getData(ForestryDataMaps.COMBUSTION_COOLANTS);
		helper.assertTrue(water != null, "Water is not a combustion engine coolant");
		helper.assertTrue(water.burnDuration() == Constants.ENGINE_COOLANT_VALUE_WATER, "Wrong coolant value for water");

		helper.succeed();
	}

	/**
	 * A reverse substrate stops rain rather than starting it, and its record rejects a nonzero duration.
	 * So this is also the guard that the codec did not read the two fields the wrong way round.
	 */
	@GameTest(template = "empty")
	public static void rainmakerSubstratesKeepTheirDirection(GameTestHelper helper) {
		RainmakerFuel iodine = data(CoreItems.IODINE_CHARGE.item(), ForestryDataMaps.RAINMAKER_FUELS);
		helper.assertTrue(iodine != null, "The iodine charge is not a rainmaker substrate");
		helper.assertFalse(iodine.reverse(), "The iodine charge stops rain instead of causing it");
		helper.assertTrue(iodine.duration() == 10000, "Wrong shower duration for the iodine charge");

		RainmakerFuel dissipation = data(CoreItems.DISSIPATION_CHARGE.item(), ForestryDataMaps.RAINMAKER_FUELS);
		helper.assertTrue(dissipation != null, "The dissipation charge is not a rainmaker substrate");
		helper.assertTrue(dissipation.reverse(), "The dissipation charge causes rain instead of stopping it");

		helper.succeed();
	}

	/**
	 * Both of these are read through a manager rather than off the holder, and each falls back to a
	 * deprecated plugin registration. So this fails if the data map stopped winning over the fallback.
	 */
	@GameTest(template = "empty")
	public static void managersReadTheirDataMaps(GameTestHelper helper) {
		float jelly = IForestryApi.INSTANCE.getHiveManager().getSwarmingMaterialChance(ApicultureItems.ROYAL_JELLY.item());
		helper.assertTrue(jelly == 0.01f, "Royal jelly resolved a swarm chance of " + jelly + " instead of 0.01");
		float paper = IForestryApi.INSTANCE.getHiveManager().getSwarmingMaterialChance(Items.PAPER);
		helper.assertTrue(paper == 0f, "Paper resolved a swarm chance of " + paper + ", so the data map matches items it should not");

		// Registered by the farms jar, so this also covers a data map declared in base and registered elsewhere
		int fertilizer = IForestryApi.INSTANCE.getFarmingManager().getFertilizeValue(CoreItems.FERTILIZER_COMPOUND.stack());
		helper.assertTrue(fertilizer == 500, "Fertilizer resolved a farm value of " + fertilizer + " instead of 500");
		int dirt = IForestryApi.INSTANCE.getFarmingManager().getFertilizeValue(new ItemStack(Items.DIRT));
		helper.assertTrue(dirt == 0, "Dirt resolved a farm fertilizer value of " + dirt);

		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void nonFuelsResolveNothing(GameTestHelper helper) {
		helper.assertTrue(data(Items.PAPER, ForestryDataMaps.FERMENTER_FUELS) == null, "Paper read as a fermenter fuel");
		helper.assertTrue(data(Items.PAPER, ForestryDataMaps.MOISTENER_FUELS) == null, "Paper read as a moistener resource");
		helper.assertTrue(data(Items.PAPER, ForestryDataMaps.PEAT_FUELS) == null, "Paper read as a peat engine fuel");
		helper.assertTrue(Fluids.LAVA.builtInRegistryHolder().getData(ForestryDataMaps.BIOGAS_FUELS) == null, "Lava read as a biogas engine fuel");
		helper.succeed();
	}

	private static <V> V data(Item item, DataMapType<Item, V> type) {
		return item.builtInRegistryHolder().getData(type);
	}

	private static void checkItems(List<String> empty, String name, DataMapType<Item, ?> type) {
		if (BuiltInRegistries.ITEM.getDataMap(type).isEmpty()) {
			empty.add(name);
		}
	}

	private static void checkFluids(List<String> empty, String name, DataMapType<Fluid, ?> type) {
		if (BuiltInRegistries.FLUID.getDataMap(type).isEmpty()) {
			empty.add(name);
		}
	}
}
