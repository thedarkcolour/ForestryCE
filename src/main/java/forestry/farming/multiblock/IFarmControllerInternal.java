package forestry.farming.multiblock;

import forestry.api.climate.IClimateProvider;
import forestry.api.multiblock.IFarmController;
import forestry.core.circuits.ISocketable;
import forestry.core.fluids.ITankManager;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.IMultiblockControllerInternal;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;
import forestry.cultivation.IFarmHousingInternal;
import forestry.farming.gui.IFarmLedgerDelegate;

public interface IFarmControllerInternal extends IFarmController, IMultiblockControllerInternal, ISocketable, IClimateProvider, IOwnedTile, IStreamableGui, IFarmHousingInternal {
	IFarmLedgerDelegate getFarmLedgerDelegate();

	IInventoryAdapter getInternalInventory();

	@Override
	ITankManager getTankManager();
}
