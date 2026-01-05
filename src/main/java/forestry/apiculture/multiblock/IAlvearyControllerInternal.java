package forestry.apiculture.multiblock;

import forestry.api.climate.IClimateProvider;
import forestry.api.multiblock.IAlvearyController;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.IMultiblockControllerInternal;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;

public interface IAlvearyControllerInternal extends IAlvearyController, IMultiblockControllerInternal, IClimateProvider, IOwnedTile, IStreamableGui {
	IInventoryAdapter getInternalInventory();

	int getHealthScaled(int i);
}
