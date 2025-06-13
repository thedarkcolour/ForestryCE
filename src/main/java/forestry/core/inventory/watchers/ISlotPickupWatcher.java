package forestry.core.inventory.watchers;

import net.minecraft.world.entity.player.Player;

// todo figure out why this isn't used by SlotWatched
public interface ISlotPickupWatcher {
	void onTake(int slotIndex, Player player);
}
