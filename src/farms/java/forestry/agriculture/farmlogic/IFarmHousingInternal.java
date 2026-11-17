package forestry.agriculture.farmlogic;

import forestry.api.core.climate.IClimateProvider;
import forestry.api.agriculture.IFarmHousing;
import forestry.core.platform.fluids.ITankManager;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.agriculture.multifarm.multiblock.IFarmInventoryInternal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;

public interface IFarmHousingInternal extends IFarmHousing, ILiquidTankTile, IClimateProvider {
	@Override
	ITankManager getTankManager();

	@Override
	IFarmInventoryInternal getFarmInventory();

	void setUpFarmlandTargets(Map<Direction, List<FarmTarget>> targets);

	BlockPos getTopCoord();

	// called by a farm logic that needs to place soil but has none of its soil resources
	void markMissingSoil();
}
