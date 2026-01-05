package forestry.core.tiles;

public enum TemperatureState {

	UNKNOWN,
	COOL,
	WARMED_UP,
	OPERATING_TEMPERATURE,
	RUNNING_HOT,
	OVERHEATING,
	MELTING;

	public static TemperatureState getState(double heat, double maxHeat) {
		final double scaledHeat = heat / maxHeat;

		if (scaledHeat < 0.20) {
			return TemperatureState.COOL;
		} else if (scaledHeat < 0.45) {
			return TemperatureState.WARMED_UP;
		} else if (scaledHeat < 0.65) {
			return TemperatureState.OPERATING_TEMPERATURE;
		} else if (scaledHeat < 0.85) {
			return TemperatureState.RUNNING_HOT;
		} else if (scaledHeat < 1.0) {
			return TemperatureState.OVERHEATING;
		} else {
			return TemperatureState.MELTING;
		}
	}

}
