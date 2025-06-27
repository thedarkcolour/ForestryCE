package forestry.core.tiles;

import net.minecraft.world.item.ItemStack;

public interface IItemStackDisplay {
	/**
	 * Called on the client to sync the displayed item from the server.
	 */
	void handleItemStackForDisplay(ItemStack stack);
}
