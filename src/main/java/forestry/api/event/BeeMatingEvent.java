package forestry.api.event;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.bee.IBee;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired before a queen is created as a result of breeding a princess and a drone.
 * For example, this can be used to cancel the breeding or change the resultant queen's genome (ex. to make Zombified).
 */
public class BeeMatingEvent extends Event implements ICancellableEvent {
	private final IBeeHousing housing;
	private IBee princess;
	private final IBee drone;

	public BeeMatingEvent(IBeeHousing housing, IBee princess, IBee drone) {
		this.housing = housing;
		this.princess = princess;
		this.drone = drone;
	}

	public IBeeHousing getHousing() {
		return this.housing;
	}

	public IBee getPrincess() {
		return this.princess;
	}

	/**
	 * Used to override the princess individual, which will become the queen individual.
	 *
	 * @param princess The new princess individual to replace the queen with.
	 */
	public void setPrincess(IBee princess) {
		this.princess = princess;
	}

	public IBee getDrone() {
		return this.drone;
	}
}
