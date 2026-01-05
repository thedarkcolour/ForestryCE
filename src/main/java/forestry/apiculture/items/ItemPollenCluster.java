package forestry.apiculture.items;

import forestry.api.core.ItemGroups;
import forestry.core.items.ItemOverlay;

public class ItemPollenCluster extends ItemOverlay {
	public ItemPollenCluster(EnumPollenCluster type) {
		super(ItemGroups.tabApiculture, type);
	}
}
