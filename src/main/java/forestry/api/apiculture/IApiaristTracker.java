package forestry.api.apiculture;

import forestry.api.apiculture.bee.IBee;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpeciesType;

/**
 * Can be used to garner information on bee breeding. See {@link ISpeciesType} for retrieval functions.
 */
public interface IApiaristTracker extends IBreedingTracker {
	/**
	 * Register the birth of a queen. Will mark species as discovered.
	 *
	 * @param queen Created queen.
	 */
	void registerQueen(IBee queen);

	/**
	 * @return Amount of queens bred with this tracker.
	 */
	int getQueenCount();

	/**
	 * Register the birth of a princess. Will mark species as discovered.
	 *
	 * @param princess Created princess.
	 */
	void registerPrincess(IBee princess);

	/**
	 * @return Amount of princesses bred with this tracker.
	 */
	int getPrincessCount();

	/**
	 * Register the birth of a drone. Will mark species as discovered.
	 *
	 * @param drone Created drone.
	 */
	void registerDrone(IBee drone);

	/**
	 * @return Amount of drones bred with this tracker.
	 */
	int getDroneCount();
}
