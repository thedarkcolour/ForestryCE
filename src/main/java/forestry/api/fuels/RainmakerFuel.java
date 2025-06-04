package forestry.api.fuels;

import com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value for items usable by the Rainmaker machine.
 *
 * @param duration Duration of the rain shower triggered by this substrate in Minecraft ticks. Must be 0 for clear weather.
 * @param speed    Speed of activation sequence triggered.
 * @param reverse  Whether the substrate stops rain instead of creating rain.
 */
public record RainmakerFuel(int duration, float speed, boolean reverse) {
	public static final Codec<RainmakerFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.INT.fieldOf("duration").forGetter(RainmakerFuel::duration),
		Codec.FLOAT.fieldOf("speed").forGetter(RainmakerFuel::speed),
		Codec.BOOL.fieldOf("reverse").forGetter(RainmakerFuel::reverse)
	).apply(inst, RainmakerFuel::new));

	public RainmakerFuel {
		if (reverse) {
			Preconditions.checkArgument(duration == 0);
		}
	}
}
