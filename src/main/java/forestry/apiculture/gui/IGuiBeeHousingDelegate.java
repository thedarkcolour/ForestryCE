package forestry.apiculture.gui;

import forestry.api.climate.IClimateProvider;
import forestry.api.core.IErrorLogicSource;
import forestry.core.owner.IOwnedTile;
import forestry.core.tiles.ITitled;

public interface IGuiBeeHousingDelegate extends ITitled, IErrorLogicSource, IOwnedTile, IClimateProvider {
	/**
	 * Returns scaled queen health or breeding progress
	 */
	int getHealthScaled(int i);

	String getHintKey();
}
