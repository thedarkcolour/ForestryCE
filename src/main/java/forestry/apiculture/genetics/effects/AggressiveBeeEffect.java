package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeProtection;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.core.features.CoreDamageTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class AggressiveBeeEffect extends ThrottledBeeEffect {
	public AggressiveBeeEffect() {
		super(true, 40, false, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<LivingEntity> entities = ThrottledBeeEffect.getEntitiesInRange(genome, housing, LivingEntity.class);
		for (LivingEntity entity : entities) {
			int damage = 4;

			// Entities are not attacked if they wear a full set of apiarist's armor.
            int count = IBeeProtection.getBeeProtectionLevel(entity, this, true);
			damage -= count;
			if (damage <= 0) {
				continue;
			}

			entity.hurt(CoreDamageTypes.source(housing.getLevel(), CoreDamageTypes.AGGRESSIVE), damage);
		}

		return storedData;
	}
}
