package forestry.apiculture;

import net.minecraft.core.BlockPos;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.LightPreference;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;

public class CrepuscularActivityType implements IActivityType {
	@Override
	public boolean isDominant() {
		return true;
	}

	@Override
	public boolean isActive(long gameTime, long dayTime, BlockPos pos) {
		int time = (int) (dayTime % 24000);
		return (0 <= time && time < 1000) || (12000 <= time && time < 13000);
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
