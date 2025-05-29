package forestry.core.genetics.mutations;

public enum EnumMutateChance {
	NONE(0xffffff),
	LOWEST(0xffba77),
	LOW(0xfffd77),
	NORMAL(0xfffd77),
	HIGH(0xfffd77),
	HIGHER(0xbeff77),
	HIGHEST(0x7bff77);

	public final int colour;

	EnumMutateChance(int colour) {
		this.colour = colour;
	}

	public static EnumMutateChance rateChance(float percent) {
		int percentInt = Math.round(percent * 100);

		if (percentInt >= 20) {
			return HIGHEST;
		} else if (percentInt >= 15) {
			return HIGHER;
		} else if (percentInt >= 12) {
			return HIGH;
		} else if (percentInt >= 10) {
			return NORMAL;
		} else if (percentInt >= 5) {
			return LOW;
		} else {
			return LOWEST;
		}
	}
}
