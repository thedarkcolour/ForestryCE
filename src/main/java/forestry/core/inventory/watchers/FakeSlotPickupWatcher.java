package forestry.core.inventory.watchers;

import net.minecraft.world.entity.player.Player;

public enum FakeSlotPickupWatcher implements ISlotPickupWatcher {
	INSTANCE;

	@Override
	public void onTake(int slotIndex, Player player) {
	}
}
