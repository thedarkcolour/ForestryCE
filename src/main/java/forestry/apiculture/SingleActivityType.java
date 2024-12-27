package forestry.apiculture;

import net.minecraft.core.BlockPos;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.LightPreference;
import forestry.api.core.IError;

import org.jetbrains.annotations.Nullable;

/**
 * A bee who has a single period of activity per day. Used by Diurnal, Nocturnal, and Metaturnal.
 * Day ticks are in the range [0,24000). 0 is the start of sunrise, 24000 is the end of nighttime.
 *
 * @param startTick The beginning day tick of activity. Inclusive lower bound.
 * @param endTick   The end day tick of activity. Exclusive upper bound.
 * @param error     The error to show in the hive GUI when this bee is inactive.
 */
public record SingleActivityType(int startTick, int endTick, IError error, LightPreference preference) implements IActivityType {
	@Override
	public boolean isDominant() {
		return true;
	}

	@Override
	public boolean isActive(long gameTime, long dayTime, BlockPos pos) {
		int time = (int) (dayTime % 24000);
		return this.startTick <= time && time < this.endTick;
	}

	@Override
	public IError getInactiveError(long gameTime, long dayTime, BlockPos pos) {
		return this.error;
	}

	@Override
	public LightPreference getLightPreference() {
		return this.preference;
	}
}
