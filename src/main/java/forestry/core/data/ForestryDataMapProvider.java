package forestry.core.data;

import java.util.concurrent.CompletableFuture;

import forestry.api.fuels.BiogasEngineFuel;
import forestry.api.fuels.PeatEngineFuel;
import forestry.core.fluids.ForestryFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import forestry.api.ForestryDataMaps;
import forestry.api.fuels.RainmakerFuel;
import forestry.core.features.CoreItems;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.DataMapProvider;

class ForestryDataMapProvider extends DataMapProvider {
	ForestryDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void gather(HolderLookup.Provider registries) {
		builder(ForestryDataMaps.RAINMAKER_FUELS)
			.add(CoreItems.IODINE_CHARGE.holder(), new RainmakerFuel(10000, 0.01f, false), false)
			.add(CoreItems.DISSIPATION_CHARGE.holder(), new RainmakerFuel(0, 0.075f, true), false)
		;

		builder(ForestryDataMaps.BIOGAS_FUELS)
			.add(ForestryFluids.BIOMASS.holder(), new BiogasEngineFuel(50, 2500, 1), false)
			.add(Fluids.WATER.builtInRegistryHolder(), new BiogasEngineFuel(10, 1000, 3), false)
			.add(NeoForgeMod.MILK, new BiogasEngineFuel(10, 10000, 3), false)
			.add(ForestryFluids.SEED_OIL.holder(), new BiogasEngineFuel(30, 2500, 1), false)
			.add(ForestryFluids.HONEY.holder(), new BiogasEngineFuel(20, 2500, 1), false)
			.add(ForestryFluids.JUICE.holder(), new BiogasEngineFuel(10, 2500, 1), false)
		;

		builder(ForestryDataMaps.PEAT_FUELS)
			.add(CoreItems.PEAT.holder(), new PeatEngineFuel(20, 2500), false)
			.add(CoreItems.BITUMINOUS_PEAT.holder(), new PeatEngineFuel(40, 3000), false)
		;
	}
}
