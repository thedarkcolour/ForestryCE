package forestry.core.tiles;

import forestry.api.core.ILocationProvider;
import forestry.core.fluids.ITankManager;

public interface ILiquidTankTile extends ILocationProvider {
	ITankManager getTankManager();
}
