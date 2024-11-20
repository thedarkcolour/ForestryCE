package forestry.core.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;

import forestry.api.core.ItemGroups;

public class ItemProperties extends Item.Properties {
	public int burnTime = -1;

	public ItemProperties burnTime(int burnTime) {
		this.burnTime = burnTime;
		return this;
	}
}
