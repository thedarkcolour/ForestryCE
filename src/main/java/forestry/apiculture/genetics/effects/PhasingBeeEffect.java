package forestry.apiculture.genetics.effects;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;

public class PhasingBeeEffect extends ThrottledBeeEffect {
	public PhasingBeeEffect() {
		super(true, 40, true, true);
	}

	@Override
	IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		ServerLevel level = (ServerLevel) housing.getWorldObj();
		RandomSource random = level.random;
		List<LivingEntity> list = getEntitiesInRange(genome, housing, LivingEntity.class);

		for (LivingEntity entity : list) {
			int count = BeeManager.armorApiaristHelper.wearsItems(entity, this, true);

			if (count >= 4) {
				continue;
			}

			double x = entity.getX();
			double y = entity.getY();
			double z = entity.getZ();

			for (int i = 0; i < 16; i++) {
				double targetX = x + (random.nextDouble() - 0.5) * 16;
				double targetY = Mth.clamp(y + (double) (random.nextInt(16) - 8), level.getMinBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight() - 1);
				double targetZ = z + (random.nextDouble() - 0.5) * 16;

				if (entity.isPassenger()) {
					entity.stopRiding();
				}

				Vec3 vec3 = entity.position();
				level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
				ChorusFruit event = ForgeEventFactory.onChorusFruitTeleport(entity, targetX, targetY, targetZ);

				if (event.isCanceled()) {
					break;
				}

				if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
					SoundEvent sound = entity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
					level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 1f, 1f);
					entity.playSound(sound, 1f, 1f);
					break;
				}
			}
		}
		return storedData;
	}

	@Override
	public IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();
		level.addParticle(ParticleTypes.PORTAL, housing.getCoordinates().getX() + 0.5, housing.getCoordinates().getY() + 0.5 + level.random.nextDouble() * 2, housing.getCoordinates().getZ() + 0.5, level.random.nextGaussian(), 0, level.random.nextGaussian());
		return storedData;
	}
}
