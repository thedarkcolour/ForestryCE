package forestry.api.core.machines.fuels;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value for items usable as a substrate by the Rainmaker.
 *
 * @param duration Duration of the rain shower triggered by this substrate in ticks. Must be 0 when reverse
 * @param speed    Speed of the activation sequence triggered
 * @param reverse  Whether the substrate stops rain instead of creating rain
 */
public record RainmakerSubstrate(int duration, float speed, boolean reverse) {
	public static final Codec<RainmakerSubstrate> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("duration").forGetter(RainmakerSubstrate::duration),
			Codec.FLOAT.fieldOf("speed").forGetter(RainmakerSubstrate::speed),
			Codec.BOOL.fieldOf("reverse").forGetter(RainmakerSubstrate::reverse)
	).apply(inst, RainmakerSubstrate::new));

	public RainmakerSubstrate {
		if (reverse) {
			Preconditions.checkArgument(duration == 0, "A reverse rainmaker fuel must have a duration of 0");
		}
	}
}
