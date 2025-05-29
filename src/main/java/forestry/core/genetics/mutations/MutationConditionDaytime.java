package forestry.core.genetics.mutations;

import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class MutationConditionDaytime implements IMutationCondition {
	private final boolean daytime;

	public MutationConditionDaytime(boolean daytime) {
		this.daytime = daytime;
	}

	@Override
	public float modifyChance(Level level, BlockPos pos, IMutation<?> mutation, IGenome genome0, IGenome genome1, IClimateProvider climate, float currentChance) {
		if (level.isDay() == this.daytime) {
			return currentChance;
		}
		return 0f;
	}

	@Override
	public Component getDescription() {
		if (this.daytime) {
			return Component.translatable("for.mutation.condition.daytime.day");
		} else {
			return Component.translatable("for.mutation.condition.daytime.night");
		}
	}
}
