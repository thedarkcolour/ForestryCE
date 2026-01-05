package forestry.core.gui.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotWorking extends SlotForestry {
	public SlotWorking(Container iinventory, int i, int j, int k) {
		super(iinventory, i, j, k);
		blockShift();
	}

	@Override
	public boolean mayPlace(ItemStack itemstack) {
		return false;
	}

	@Override
	public void onTake(Player player, ItemStack itemStack) {
	}

	@Override
	public boolean mayPickup(Player stack) {
		return false;
	}

	@Override
	public ItemStack remove(int i) {
		return ItemStack.EMPTY;
	}
}
