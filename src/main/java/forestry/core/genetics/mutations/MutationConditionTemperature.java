package forestry.core.genetics.mutations;

import forestry.api.climate.IClimateProvider;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ClimateHelper;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class MutationConditionTemperature implements IMutationCondition {
	private final TemperatureType minTemperature;
	private final TemperatureType maxTemperature;

	public MutationConditionTemperature(TemperatureType minTemperature, TemperatureType maxTemperature) {
		this.minTemperature = minTemperature;
		this.maxTemperature = maxTemperature;
	}

	@Override
	public float modifyChance(Level level, BlockPos pos, IMutation<?> mutation, IGenome genome0, IGenome genome1, IClimateProvider climate, float currentChance) {
		TemperatureType biomeTemperature = climate.temperature();

		if (biomeTemperature.ordinal() < this.minTemperature.ordinal() || biomeTemperature.ordinal() > this.maxTemperature.ordinal()) {
			return 0f;
		}
		return currentChance;
	}

	@Override
	public Component getDescription() {
		Component minString = ClimateHelper.toDisplay(this.minTemperature);

		if (this.minTemperature != this.maxTemperature) {
			Component maxString = ClimateHelper.toDisplay(this.maxTemperature);
			return Component.translatable("for.mutation.condition.temperature.range", minString, maxString);
		} else {
			return Component.translatable("for.mutation.condition.temperature.single", minString);
		}
	}
}
