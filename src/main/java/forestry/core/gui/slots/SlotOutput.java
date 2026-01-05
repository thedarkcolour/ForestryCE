package forestry.core.gui.slots;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SlotOutput extends SlotWatched {
	public SlotOutput(Container iinventory, int slotIndex, int posX, int posY) {
		super(iinventory, slotIndex, posX, posY);
	}

	@Override
	public boolean mayPlace(ItemStack itemstack) {
		return false;
	}
}
