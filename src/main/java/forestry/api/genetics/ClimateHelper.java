package forestry.api.genetics;

import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.core.ToleranceType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public class ClimateHelper {
	public static int getColor(TemperatureType temperature) {
		return switch (temperature) {
			case ICY -> 0xe6e6fa;
			case COLD -> 0x31698a;
			case NORMAL -> 0xf0e9cc;
			case WARM -> 0xcd9b1d;
			case HOT -> 0xdf512e;
			case HELLISH -> 0x9c433e;
		};
	}

	public static boolean isWithinLimits(TemperatureType temperature, HumidityType humidity, TemperatureType idealTemp, ToleranceType temperatureTolerance, HumidityType idealHumidity, ToleranceType humidityTolerance) {
		return isWithinLimits(temperature, idealTemp, temperatureTolerance) && isWithinLimits(humidity, idealHumidity, humidityTolerance);
	}

	public static boolean isWithinLimits(TemperatureType temperature, TemperatureType idealTemp, ToleranceType tolerance) {
		TemperatureType max = idealTemp.up(tolerance.up);
		TemperatureType min = idealTemp.down(tolerance.down);
		return temperature.isWarmerOrEqual(min) && temperature.isCoolerOrEqual(max);
	}

	public static boolean isWithinLimits(HumidityType humidity, HumidityType idealHumidity, ToleranceType tolerance) {
		HumidityType max = idealHumidity.up(tolerance.up);
		HumidityType min = idealHumidity.down(tolerance.down);
		return humidity.isWetterOrEqual(min) && humidity.isDrierOrEqual(max);
	}

	// todo move into TemperatureType
	public static MutableComponent toDisplay(TemperatureType temperature) {
		return Component.translatable("for.gui." + temperature.toString().toLowerCase(Locale.ENGLISH));
	}

	// todo move into HumidityType
	public static MutableComponent toDisplay(HumidityType humidity) {
		return Component.translatable("for.gui." + humidity.toString().toLowerCase(Locale.ENGLISH));
	}
}
