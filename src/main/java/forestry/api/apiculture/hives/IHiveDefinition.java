package forestry.api.apiculture.hives;

import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The definition of a wild beehive that generates naturally in the world.
 * Register in {@link forestry.api.plugin.IApicultureRegistration#registerHive}.
 */
public interface IHiveDefinition {
	/**
	 * The hive generator for this hive.
	 */
	IHivePlacement getHiveGen();

	/**
	 * The hive block to be placed in the world.
	 */
	BlockState getBlockState();

	/**
	 * returns true if the hive can be generated in these conditions.
	 * Used as a fast early-elimination check for hives that have no hope of spawning in the area.
	 */
	boolean isGoodBiome(Holder<Biome> biome);

	boolean isGoodHumidity(HumidityType humidity);

	boolean isGoodTemperature(TemperatureType temperature);

	/**
	 * float representing the relative chance a hive will generate in a chunk.
	 * Default is 1.0, higher numbers result in more hives, smaller will result in fewer.
	 * Tree hives want around 3.0 to 4.0 since there are less locations to generate on.
	 *
	 * @since 1.1.1
	 * @deprecated Generation chance is now set by {@link IHive}. This value is a default, but can be overridden.
	 */
	@Deprecated(forRemoval = true)
	float getGenChance();

	/**
	 * Called after successful hive generation.
	 * level, x, y, z give the location of the new hive.
	 **/
	void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos);
}
