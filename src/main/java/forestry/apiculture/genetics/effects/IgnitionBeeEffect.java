package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.core.render.ParticleRender;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class IgnitionBeeEffect extends ThrottledBeeEffect {
	private static final int ignitionChance = 50;
	private static final int fireDuration = 500;

	public IgnitionBeeEffect() {
		super(false, 20, false, true);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();
		List<LivingEntity> entities = ThrottledBeeEffect.getEntitiesInRange(genome, housing, LivingEntity.class);
		for (LivingEntity entity : entities) {
			int chance = ignitionChance;
			int duration = fireDuration;

			// Entities are not attacked if they wear a full set of apiarist's armor.
			int count = BeeManager.armorApiaristHelper.wearsItems(entity, this, true);
			if (count > 3) {
				continue; // Full set, no damage/effect
			} else if (count > 2) {
				chance = 5;
				duration = 50;
			} else if (count > 1) {
				chance = 20;
				duration = 200;
			} else if (count > 0) {
				chance = 35;
				duration = 350;
			}

			if (level.random.nextInt(1000) >= chance) {
				continue;
			}

			entity.setSecondsOnFire(duration);
		}

		return storedData;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		ClientLevel level = (ClientLevel) housing.getWorldObj();
		if (level.random.nextInt(2) != 0) {
			super.doFX(genome, storedData, housing);
		} else {
			Vec3 beeFXCoordinates = housing.getBeeFXCoordinates();
			ParticleRender.addEntityIgnitionFX(level, beeFXCoordinates.x, beeFXCoordinates.y + 0.5, beeFXCoordinates.z);
		}
		return storedData;
	}

}
