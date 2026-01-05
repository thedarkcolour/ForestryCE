package forestry.api.core;

/**
 * Denotes a species that has a preferred temperature and humidity.
 */
public interface IClimateSensitive {
	/**
	 * @return The preferred/ideal temperature for this species.
	 */
	TemperatureType getTemperature();

	/**
	 * @return The preferred/ideal humidity for this species.
	 */
	HumidityType getHumidity();
}
