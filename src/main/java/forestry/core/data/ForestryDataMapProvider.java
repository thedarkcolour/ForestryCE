package forestry.core.data;

import forestry.api.ForestryDataMaps;
import forestry.api.fuels.BiogasEngineFuel;
import forestry.api.fuels.PeatEngineFuel;
import forestry.api.fuels.RainmakerFuel;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreItems;
import forestry.core.fluids.ForestryFluids;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.modules.features.FeatureItem;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

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

		registerCompostables();
	}

	private void registerCompostables() {
		Object2FloatMap<Item> compostables = new Object2FloatOpenHashMap<>();

		for (FeatureItem<?> fruit : CoreItems.FRUITS.getFeatures()) {
			compostables.put(fruit.item(), 0.65f);
		}
		compostables.put(CoreItems.MOULDY_WHEAT.item(), 0.65f);
		compostables.put(CoreItems.DECAYING_WHEAT.item(), 0.65f);
		compostables.put(CoreItems.MULCH.item(), 0.65f);
		compostables.put(CoreItems.ASH.item(), 0.65f);
		compostables.put(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), 0.65f);
		compostables.put(CoreItems.PEAT.item(), 0.75f);
		compostables.put(CoreItems.COMPOST.item(), 1f);
		for (Item pollen : ApicultureItems.POLLEN_CLUSTER.getItems()) {
			compostables.put(pollen, 0.3f);
		}
		compostables.put(ArboricultureItems.SAPLING.item(), 0.3f);
		compostables.put(ArboricultureItems.POLLEN_FERTILE.item(), 0.3f);
		for (BlockItem leaves : ArboricultureBlocks.LEAVES_DECORATIVE.getItems()) {
			compostables.put(leaves, 0.3f);
		}
		compostables.put(LepidopterologyItems.COCOON_GE.item(), 0.3f);

		Builder<Compostable, Item> compostablesDataMap = builder(NeoForgeDataMaps.COMPOSTABLES);
		compostables.forEach((item, chance) -> compostablesDataMap.add(item.builtInRegistryHolder(), new Compostable(chance), false));
	}
}
