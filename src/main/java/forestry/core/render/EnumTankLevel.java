package forestry.core.render;

import net.minecraftforge.fluids.IFluidTank;

public enum EnumTankLevel {
	EMPTY(0),
	LOW(25),
	MEDIUM(50),
	HIGH(75),
	MAXIMUM(100);

	private final int level;

	EnumTankLevel(int level) {
		this.level = level;
	}

	public int getLevelScaled(int scale) {
		return this.level * scale / 100;
	}

	public static EnumTankLevel rateTankLevel(IFluidTank tank) {
		return rateTankLevel(100 * tank.getFluidAmount() / tank.getCapacity());
	}

	public static EnumTankLevel rateTankLevel(int scaled) {

		if (scaled < 5) {
			return EMPTY;
		} else if (scaled < 30) {
			return LOW;
		} else if (scaled < 60) {
			return MEDIUM;
		} else if (scaled < 90) {
			return HIGH;
		} else {
			return MAXIMUM;
		}
	}
}
