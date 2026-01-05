package forestry.apiculture;

import forestry.api.apiculture.IBeeHousing;
import forestry.apiculture.inventory.IApiaryInventory;

public interface IApiary extends IBeeHousing {
	IApiaryInventory getApiaryInventory();
}
