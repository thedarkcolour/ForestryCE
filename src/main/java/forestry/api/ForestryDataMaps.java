package forestry.api;

import com.mojang.serialization.Codec;
import forestry.api.fuels.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class ForestryDataMaps {
	/**
	 * Add new fuels for the fermenter here (i.e. fertilizer)
	 */
	public static final DataMapType<Item, FermenterFuel> FERMENTER_FUELS = create("fermenter_fuels", Registries.ITEM, FermenterFuel.CODEC)
		.build();

	/**
	 * Add new resources for the moistener here (i.e. wheat)
	 */
	public static final DataMapType<Item, MoistenerFuel> MOISTENER_FUELS = create("moistener_fuels", Registries.ITEM, MoistenerFuel.CODEC)
		.build();

	/**
	 * Add new substrates for the rainmaker here
	 */
	public static final DataMapType<Item, RainmakerFuel> RAINMAKER_FUELS = create("rainmaker_fuels", Registries.ITEM, RainmakerFuel.CODEC)
		.synced(RainmakerFuel.CODEC, true)
		.build();

	/**
	 * Add new fuels for the Biogas Engine here
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> BIOGAS_FUELS = create("biogas_fuels", Registries.FLUID, BiogasEngineFuel.CODEC)
		.build();

	/**
	 * Add new fuels for the Peat Engine here
	 */
	public static final DataMapType<Item, PeatEngineFuel> PEAT_FUELS = create("peat_fuels", Registries.ITEM, PeatEngineFuel.CODEC)
		.build();

	/**
	 * Add new fertilizers to use in Multiblock Farms and Planters
	 */
	public static final DataMapType<Item, Integer> FARM_FERTILIZERS = create("farm_fertilizers", Registries.ITEM, Codec.INT)
		.build();

	/**
	 * Add new items to be used as feed for the Alveary Swarmer
	 */
	public static final DataMapType<Item, Integer> SWARMER_FEED = create("swarmer_feed", Registries.ITEM, Codec.INT)
		.build();

	private static <K, V> DataMapType.Builder<V, K> create(String id, ResourceKey<Registry<K>> registry, Codec<V> codec) {
		return DataMapType.builder(ForestryConstants.forestry(id), registry, codec);
	}
}
