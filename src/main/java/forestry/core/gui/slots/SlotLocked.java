package forestry.core.gui.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotLocked extends SlotForestry {

	public SlotLocked(Container inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		setCanAdjustPhantom(false);
		blockShift();
		setPhantom();
	}

	@Override
	public void onTake(Player player, ItemStack itemStack) {
	}

	@Override
	public boolean mayPlace(ItemStack par1ItemStack) {
		return false;
	}

	@Override
	public ItemStack remove(int i) {
		return ItemStack.EMPTY;
	}
}
