package forestry.apiculture.inventory;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;

public interface IApiaryInventory extends IBeeHousingInventory {
	void wearOutFrames(IBeeHousing beeHousing, int amount);
}
