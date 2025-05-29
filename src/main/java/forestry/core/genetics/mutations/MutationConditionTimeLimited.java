package forestry.core.genetics.mutations;

import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationCondition;
import forestry.core.utils.DayMonth;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class MutationConditionTimeLimited implements IMutationCondition {
	private final DayMonth start;
	private final DayMonth end;

	public MutationConditionTimeLimited(int startMonth, int startDay, int endMonth, int endDay) {
		this.start = new DayMonth(startDay, startMonth);
		this.end = new DayMonth(endDay, endMonth);
	}

	@Override
	public float modifyChance(Level level, BlockPos pos, IMutation<?> mutation, IGenome genome0, IGenome genome1, IClimateProvider climate, float currentChance) {
		DayMonth now = DayMonth.now();

		if (now.between(this.start, this.end)) {
			return currentChance;
		}

		return 0;
	}

	@Override
	public Component getDescription() {
		return Component.translatable("for.mutation.condition.date", this.start.getDisplayName(), this.end.getDisplayName());
	}
}
