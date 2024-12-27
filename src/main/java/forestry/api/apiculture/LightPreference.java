package forestry.api.apiculture;

/**
 * Used in {@link IActivityType} to determine whether a bee prefers high light levels or low light levels.
 *
 * @see IActivityType#getLightPreference
 */
public enum LightPreference {
	/**
	 * Light levels 12-15.
	 */
	LIGHT,
	/**
	 * Light levels 0-11.
	 */
	DARK,
	/**
	 * All light levels.
	 */
	ANY
}
