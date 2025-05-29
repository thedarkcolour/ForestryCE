package forestry.api.event;

import forestry.api.genetics.IBreedingTracker;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class SyncedBreedingTrackerEvent extends Event {
	public final IBreedingTracker tracker;
	public final Player player;

	public SyncedBreedingTrackerEvent(IBreedingTracker tracker, Player player) {
		this.tracker = tracker;
		this.player = player;
	}
}
