package forestry.api.apiculture.bee;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.bee.IBeeSpecies;
import forestry.api.genetics.IGenome;

/**
 * Determines whether a bee species is jubilant in a certain environment.
 */
public interface IBeeJubilance {
	/**
	 * Returns true when conditions are right to make this species Jubilant.
	 * Jubilant bees can produce their Specialty products.
	 */
	boolean isJubilant(IBeeSpecies species, IGenome genome, IBeeHousing housing);
}
