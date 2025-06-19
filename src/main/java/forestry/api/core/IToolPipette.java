package forestry.api.core;

import net.minecraft.world.item.ItemStack;

public interface IToolPipette {
	/**
	 * @return true if the pipette can pipette.
	 */
	boolean canPipette(ItemStack pipette);
}
