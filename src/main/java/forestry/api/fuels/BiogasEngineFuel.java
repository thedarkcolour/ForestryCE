package forestry.api.fuels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * @param powerPerCycle         Power produced by this fuel per work cycle of the engine.
 * @param burnDuration          How many work cycles a single "stack" of this type lasts.
 * @param dissipationMultiplier By how much the normal heat dissipation rate of 1 is multiplied when using this fuel type.
 */
public record BiogasEngineFuel(int powerPerCycle, int burnDuration, int dissipationMultiplier) {
	public static final Codec<BiogasEngineFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.INT.fieldOf("power_per_cycle").forGetter(BiogasEngineFuel::powerPerCycle),
		Codec.INT.fieldOf("burn_duration").forGetter(BiogasEngineFuel::burnDuration),
		Codec.INT.fieldOf("dissipation_multiplier").forGetter(BiogasEngineFuel::dissipationMultiplier)
	).apply(inst, BiogasEngineFuel::new));
}
