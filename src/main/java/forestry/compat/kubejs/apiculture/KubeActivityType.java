package forestry.compat.kubejs.apiculture;

import forestry.api.apiculture.bee.IActivityType;
import forestry.api.apiculture.bee.LightPreference;
import forestry.api.core.IError;
import net.minecraft.core.BlockPos;

public record KubeActivityType(IsActiveFunction isActive, InactiveErrorFunction inactiveErrorFunction,
							   LightPreference lightPreference, boolean dominant) implements IActivityType {
	@Override
	public boolean isActive(long gameTime, long dayTime, BlockPos pos) {
		return this.isActive.isActive(gameTime, dayTime, pos);
	}

	@Override
	public IError getInactiveError(long gameTime, long dayTime, BlockPos pos) {
		return this.inactiveErrorFunction.getInactiveError(gameTime, dayTime, pos);
	}

	@Override
	public LightPreference getLightPreference() {
		return this.lightPreference;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	public interface IsActiveFunction {
		boolean isActive(long gameTime, long dayTime, BlockPos pos);
	}

	public interface InactiveErrorFunction {
		IError getInactiveError(long gameTime, long dayTime, BlockPos pos);
	}
}
