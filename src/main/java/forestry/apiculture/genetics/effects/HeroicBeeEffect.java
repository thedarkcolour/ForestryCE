package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.core.damage.CoreDamageTypes;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;

public class HeroicBeeEffect extends ThrottledBeeEffect {
	public HeroicBeeEffect() {
		super(false, 40, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<Monster> mobs = ThrottledBeeEffect.getEntitiesInRange(genome, housing, Monster.class);
		for (Monster mob : mobs) {
			mob.hurt(CoreDamageTypes.source(housing.getWorldObj(), CoreDamageTypes.HEROIC), 2);
		}

		return storedData;
	}
}
