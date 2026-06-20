package forestry.apiculture.multiblock;

import forestry.api.climate.IClimateProvider;
import forestry.api.multiblock.IAlvearyController;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;

/**
 * Machine-specific surface of the alveary controller. After the engine rewrite (plan Task 2.2) this
 * extends the <b>public</b> {@link IAlvearyController} (which extends the public
 * {@code forestry.api.multiblock.IMultiblockController}) rather than the deleted engine-internal
 * {@code IMultiblockControllerInternal}; only the alveary-specific accessors remain (climate, bee
 * listeners/modifiers, beekeeping logic, shared inventory, GUI streaming).
 */
public interface IAlvearyControllerInternal extends IAlvearyController, IClimateProvider, IOwnedTile, IStreamableGui {
	IInventoryAdapter getInternalInventory();

	int getHealthScaled(int i);
}
