package forestry.core.tiles;

import forestry.core.render.TankRenderInfo;

public interface IRenderableTile {
	TankRenderInfo getResourceTankInfo();

	TankRenderInfo getProductTankInfo();
}
