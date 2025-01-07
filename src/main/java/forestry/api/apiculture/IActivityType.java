package forestry.api.apiculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelData;

import forestry.api.core.IError;
import forestry.api.genetics.alleles.IRegistryAlleleValue;

/**
 * Used to define the active hours of a bee.
 *
 * @see ForestryActivityTypes For the default activity types added by Forestry.
 */
public interface IActivityType extends IRegistryAlleleValue {
	/**
	 * Determines whether this bee can be active and work at the current time of day.
	 *
	 * @param gameTime The number of ticks spent in the current level. See {@link LevelData#getGameTime}.
	 * @param dayTime  The time of day of the current level, can be above 24000. See {@link LevelData#getDayTime}.
	 * @param pos      The hive position. It can be used for randomness, like how flowers are offset based on position.
	 * @return {@code true} if this bee should be able to work, {@code false} if this bee should be asleep.
	 * @see <a href="https://minecraft.wiki/w/Java_Edition_level_format#level.dat_format">level.dat format - Minecraft Wiki</a>
	 */
	boolean isActive(long gameTime, long dayTime, BlockPos pos);

	/**
	 * Gets the error to display in GUIs for when this bee is inactive. This method is only called when
	 * {@link #isActive} returns false for the given parameters, so no need to duplicate your time checks.
	 *
	 * @param gameTime The number of ticks spent in the current level. See {@link LevelData#getGameTime}.
	 * @param dayTime  The time of day of the current level, can be above 24000. See {@link LevelData#getDayTime}.
	 * @param pos      The hive position. It can be used for randomness, like how flowers are offset based on position.
	 * @return A descriptive error indicating that the time of day is wrong for this bee.
	 */
	IError getInactiveError(long gameTime, long dayTime, BlockPos pos);

	/**
	 * Whether a bee only works above light level 11, less than or equal to that, or doesn't care.
	 * For example, Diurnal bees want to work when the hive is above light level 11, but nocturnal bees will only work
	 * when the light level is 11 or below.
	 *
	 * @return The preference of light level for this activity type.
	 */
	LightPreference getLightPreference();
}
