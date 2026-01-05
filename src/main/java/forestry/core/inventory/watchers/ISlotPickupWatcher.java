package forestry.core.inventory.watchers;

import net.minecraft.world.entity.player.Player;

public interface ISlotPickupWatcher {
	void onTake(int slotIndex, Player player);
}
