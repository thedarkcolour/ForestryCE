package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.entities.AIAvoidPlayers;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;

public class RepulsionBeeEffect extends ThrottledBeeEffect {
	public RepulsionBeeEffect() {
		super(false, 100, true, true);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<Monster> mobs = ThrottledBeeEffect.getEntitiesInRange(genome, housing, Monster.class);
		for (Monster mob : mobs) {
			if (!isMobAvoidingPlayers(mob)) {
				mob.goalSelector.addGoal(3, new AIAvoidPlayers(mob, 6.0f, 0.25f, 0.3f));
				mob.goalSelector.tick();    // good job Nedelosk
			}
		}

		return storedData;
	}

	private boolean isMobAvoidingPlayers(Monster mob) {
		return mob.goalSelector.getRunningGoals().anyMatch(task -> task.getGoal() instanceof AIAvoidPlayers);
	}
}
