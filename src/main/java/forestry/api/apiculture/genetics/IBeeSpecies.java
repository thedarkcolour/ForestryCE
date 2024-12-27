package forestry.api.apiculture.genetics;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.HumidityType;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ISpecies;

/**
 * Represents a bee species. Defines a bee's traits, default genome, and produce.
 */
public interface IBeeSpecies extends ISpecies<IBee>, IProductProducer, ISpecialtyProducer {
	@Override
	IBeeSpeciesType getType();

	/**
	 * @return The preferred/ideal temperature for this bee.
	 */
	TemperatureType getTemperature();

	/**
	 * @return The preferred/ideal humidity for this bee.
	 */
	HumidityType getHumidity();

	/**
	 * Determines whether a bee of this species is in a jubilant state. Bees in a jubilant state are able
	 * to produce their specialty products. Most bees enter their jubilant state when they are in their preferred
	 * temperature/humidity, but others have additional restrictions.
	 *
	 * @param genome  The genome of a bee of this species.
	 * @param housing The hive where the bee is working. Provides access to the bee's working environment.
	 * @return Whether this bee should produce its specialty products.
	 * @see forestry.api.apiculture.IBeeJubilance
	 */
	boolean isJubilant(IGenome genome, IBeeHousing housing);

	/**
	 * @return The color of the bee's body. Used for tintIndex = 1 and is usually a shade of yellow, {@code 0xffdc16}.
	 */
	int getBody();

	/**
	 * @return The color of the bee's stripes. Used for tintIndex = 2 and is usually black.
	 */
	int getStripes();

	/**
	 * @return The color of the bee's outline. Used for tintIndex = 0.
	 */
	int getOutline();
}
