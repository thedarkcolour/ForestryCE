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
import forestry.api.core.machines.fuels.RainmakerFuel;

import static forestry.api.ForestryConstants.forestry;

/**
 * All data maps added by base Forestry.
 *
 * <p>A file goes under the namespace of the data map rather than the namespace of the mod adding to
 * it, and every mod's file at that path is merged. So another mod adds an entry with a file of the
 * right shape and no dependency on Forestry.
 *
 * <p>Every map here is synced, because the client reads them to build JEI pages and to decide what a
 * slot accepts. None is synced as mandatory: a mandatory map disconnects any client that has not
 * registered it, and a jar of Forestry is optional on either side.
 * <p>
 * Ex. {@code data/forestry/data_maps/item/fermenter_fuels.json}
 */
public class ForestryDataMaps {
	/**
	 * Add new fuels for the Fermenter here (i.e. fertilizer)
	 */
	public static final DataMapType<Item, FermenterFuel> FERMENTER_FUELS = create("fermenter_fuels", Registries.ITEM, FermenterFuel.CODEC)
			.synced(FermenterFuel.CODEC, false)
			.build();

	/**
	 * Add new resources for the Moistener here (i.e. wheat)
	 */
	public static final DataMapType<Item, MoistenerFuel> MOISTENER_FUELS = create("moistener_fuels", Registries.ITEM, MoistenerFuel.CODEC)
			.synced(MoistenerFuel.CODEC, false)
			.build();

	/**
	 * Add new substrates for the Rainmaker here
	 */
	public static final DataMapType<Item, RainmakerFuel> RAINMAKER_FUELS = create("rainmaker_fuels", Registries.ITEM, RainmakerFuel.CODEC)
			.synced(RainmakerFuel.CODEC, false)
			.build();

	/**
	 * Add new fuels for the Biogas Engine here
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> BIOGAS_FUELS = create("biogas_fuels", Registries.FLUID, BiogasEngineFuel.CODEC)
			.synced(BiogasEngineFuel.CODEC, false)
			.build();

	/**
	 * Add new fuels for the Combustion Engine here
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> COMBUSTION_FUELS = create("combustion_fuels", Registries.FLUID, BiogasEngineFuel.CODEC)
			.synced(BiogasEngineFuel.CODEC, false)
			.build();

	/**
	 * Add new coolants for the Combustion Engine here. Only {@link BiogasEngineFuel#burnDuration} and
	 * {@link BiogasEngineFuel#dissipationMultiplier} are read for a coolant
	 */
	public static final DataMapType<Fluid, BiogasEngineFuel> COMBUSTION_COOLANTS = create("combustion_coolants", Registries.FLUID, BiogasEngineFuel.CODEC)
			.synced(BiogasEngineFuel.CODEC, false)
			.build();

	/**
	 * Add new fuels for the Peat-fired Engine here
	 */
	public static final DataMapType<Item, PeatEngineFuel> PEAT_FUELS = create("peat_fuels", Registries.ITEM, PeatEngineFuel.CODEC)
			.synced(PeatEngineFuel.CODEC, false)
			.build();

	/**
	 * Add new fertilizers to use in Multiblock Farms and Planters. The value is the amount of fertilizer
	 * a single item is worth. Forestry's fertilizer is worth {@code 500}
	 */
	public static final DataMapType<Item, Integer> FARM_FERTILIZERS = create("farm_fertilizers", Registries.ITEM, ExtraCodecs.POSITIVE_INT)
			.synced(ExtraCodecs.POSITIVE_INT, false)
			.build();

	/**
	 * Add new items to be used as feed for the Alveary Swarmer. The value is the chance a swarm hive is
	 * created. For Royal Jelly, this is {@code 0.01} or 1%
	 */
	public static final DataMapType<Item, Float> SWARMER_FEED = create("swarmer_feed", Registries.ITEM, Codec.FLOAT)
			.synced(Codec.FLOAT, false)
			.build();

	/**
	 * The postage an item is worth when it is attached to a letter. An item with no entry is not a
	 * stamp. Mods add their own stamps with a data map file and no dependency on Forestry.
	 *
	 * <p>Registered by the mail jar, so the data map is absent when that jar is not installed. Nothing
	 * in base Forestry reads it.
	 */
	public static final DataMapType<Item, Integer> POSTAGE = create("postage", Registries.ITEM, ExtraCodecs.POSITIVE_INT)
			.synced(ExtraCodecs.POSITIVE_INT, false)
			.build();

	private static <K, V> DataMapType.Builder<V, K> create(String id, ResourceKey<Registry<K>> registry, Codec<V> codec) {
		return DataMapType.builder(forestry(id), registry, codec);
	}
}
