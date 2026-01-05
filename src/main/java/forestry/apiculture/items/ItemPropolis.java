package forestry.apiculture.items;

import forestry.api.core.ItemGroups;
import forestry.core.items.ItemOverlay;

//TODO - create common superclass for items/blocks defined by an enum.
//Will help with automatic creation of stuff too.
public class ItemPropolis extends ItemOverlay {

	private final EnumPropolis type;

	public ItemPropolis(EnumPropolis type) {
		super(ItemGroups.tabApiculture, type);
		this.type = type;
	}
}
