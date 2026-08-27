package forestry.agriculture.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.DataMapProvider;

import forestry.api.ForestryDataMaps;
import forestry.core.features.CoreItems;

/**
 * Generates what a Multiblock Farm or Planter accepts as fertilizer. The game merges a data map
 * across every pack that names it, so another mod adds its own fertilizer with a file of this shape
 * and no dependency on Forestry.
 */
public class AgricultureDataMapProvider extends DataMapProvider {
	public AgricultureDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		super(output, lookup);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		builder(ForestryDataMaps.FARM_FERTILIZERS)
				.add(CoreItems.FERTILIZER_COMPOUND.item().builtInRegistryHolder(), 500, false);
	}

	@Override
	public String getName() {
		return "Forestry Data Maps";
	}
}
