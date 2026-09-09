package forestry.api;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.registries.datamaps.DataMapType;

import forestry.api.core.machines.fuels.BiogasEngineFuel;
import forestry.api.core.machines.fuels.FermenterFuel;
import forestry.api.core.machines.fuels.MoistenerFuel;
import forestry.api.core.machines.fuels.PeatEngineFuel;
import forestry.api.core.machines.fuels.RainmakerSubstrate;

import static forestry.api.ForestryConstants.forestry;

/**
 * All data maps added by base Forestry.
 */
public class ForestryDataMaps {
	/**
	 * Add fuels for the Fermenter here (ex. fertilizer)
	 */
	public static final DataMapType<Item, FermenterFuel> FERMENTER_FUELS = create("fermenter_fuels", Registries.ITEM, FermenterFuel.CODEC)
			.synced(FermenterFuel.CODEC, false)
			.build();

	/**
	 * Add resources for the Moistener here (ex. wheat)
	 */
	public static final DataMapType<Item, MoistenerFuel> MOISTENER_FUELS = create("moistener_fuels", Registries.ITEM, MoistenerFuel.CODEC)
			.synced(MoistenerFuel.CODEC, false)
			.build();

	/**
	 * Add substrates for the Rainmaker here
	 */
	public static final DataMapType<Item, RainmakerSubstrate> RAINMAKER_FUELS = create("rainmaker_fuels", Registries.ITEM, RainmakerSubstrate.CODEC)
			.synced(RainmakerSubstrate.CODEC, false)
			.build();

	/**
	 * Add fuels for the Biogas Engine here
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> BIOGAS_FUELS = create("biogas_fuels", Registries.FLUID, BiogasEngineFuel.CODEC)
			.synced(BiogasEngineFuel.CODEC, false)
			.build();

	/**
	 * Add fuels for the Combustion Engine here
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> COMBUSTION_FUELS = create("combustion_fuels", Registries.FLUID, BiogasEngineFuel.CODEC)
			.synced(BiogasEngineFuel.CODEC, false)
			.build();

	/**
	 * Add coolants for the Combustion Engine here. Only {@link BiogasEngineFuel#burnDuration()} and
	 * {@link BiogasEngineFuel#dissipationMultiplier()} are read for a coolant
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> COMBUSTION_COOLANTS = create("combustion_coolants", Registries.FLUID, BiogasEngineFuel.CODEC)
			.synced(BiogasEngineFuel.CODEC, false)
			.build();

	/**
	 * Add fuels for the Peat-fired Engine here
	 */
	public static final DataMapType<Item, PeatEngineFuel> PEAT_FUELS = create("peat_fuels", Registries.ITEM, PeatEngineFuel.CODEC)
			.synced(PeatEngineFuel.CODEC, false)
			.build();

	/**
	 * Add fertilizers to use in Multiblock Farms and Planters. The value is the amount of fertilizer
	 * a single item is worth. Forestry's fertilizer is worth {@code 500}
	 */
	public static final DataMapType<Item, Integer> FARM_FERTILIZERS = create("farm_fertilizers", Registries.ITEM, ExtraCodecs.POSITIVE_INT)
			.synced(ExtraCodecs.POSITIVE_INT, false)
			.build();

	/**
	 * Add items to be used as feed for the Alveary Swarmer. The value is the chance a swarm hive is
	 * created. For Royal Jelly, this is {@code 0.01} or 1%
	 */
	public static final DataMapType<Item, Float> SWARMER_FEED = create("swarmer_feed", Registries.ITEM, Codec.FLOAT)
			.synced(Codec.FLOAT, false)
			.build();

	/**
	 * The postage value of a stamp on a letter. Any item with postage>0 is usable as a stamp. Registered only when the mail jar is loaded.
	 */
	public static final DataMapType<Item, Integer> POSTAGE = create("postage", Registries.ITEM, ExtraCodecs.POSITIVE_INT)
			.synced(ExtraCodecs.POSITIVE_INT, false)
			.build();

	private static <K, V> DataMapType.Builder<V, K> create(String id, ResourceKey<Registry<K>> registry, Codec<V> codec) {
		return DataMapType.builder(forestry(id), registry, codec);
	}
}
