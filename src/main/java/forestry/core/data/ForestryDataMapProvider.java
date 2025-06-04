package forestry.core.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import forestry.api.ForestryDataMaps;
import forestry.api.fuels.RainmakerFuel;
import forestry.core.features.CoreItems;

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
	}
}
