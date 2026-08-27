package forestry.api.core.machines.fuels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value for items usable as fuel by the Fermenter.
 *
 * @param fermentPerCycle How much is fermented per work cycle, i.e. how much biomass is produced per cycle
 * @param burnDuration    Amount of work cycles a single item of this fuel lasts before expiring
 */
public record FermenterFuel(int fermentPerCycle, int burnDuration) {
	public static final Codec<FermenterFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("ferment_per_cycle").forGetter(FermenterFuel::fermentPerCycle),
			Codec.INT.fieldOf("burn_duration").forGetter(FermenterFuel::burnDuration)
	).apply(inst, FermenterFuel::new));
}
