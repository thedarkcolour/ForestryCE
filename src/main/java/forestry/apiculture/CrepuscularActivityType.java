package forestry.apiculture;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.LightPreference;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;
import net.minecraft.core.BlockPos;

public class CrepuscularActivityType implements IActivityType {
	@Override
	public boolean isDominant() {
		return true;
	}

	@Override
	public boolean isActive(long gameTime, long dayTime, BlockPos pos) {
		int time = (int) (dayTime % 24000);
		return (0 <= time && time < 2000) || (10000 <= time && time < 15000) || 21000 <= time;
	}

	@Override
	public IError getInactiveError(long gameTime, long dayTime, BlockPos pos) {
		return ForestryError.NOT_TWILIGHT;
	}

	@Override
	public LightPreference getLightPreference() {
		return LightPreference.ANY;
	}
}
