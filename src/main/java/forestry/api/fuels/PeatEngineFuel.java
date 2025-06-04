package forestry.api.fuels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * @param powerPerCycle Power produced by this fuel per work cycle.
 * @param burnDuration  Amount of work cycles this item lasts before being consumed.
 */
public record PeatEngineFuel(int powerPerCycle, int burnDuration) {
	public static final Codec<PeatEngineFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.INT.fieldOf("power_per_cycle").forGetter(PeatEngineFuel::powerPerCycle),
		Codec.INT.fieldOf("burn_duration").forGetter(PeatEngineFuel::burnDuration)
	).apply(inst, PeatEngineFuel::new));
}
