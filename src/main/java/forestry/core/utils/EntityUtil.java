package forestry.core.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class EntityUtil {
	@Nullable
	public static <T extends Mob> T spawnEntity(Level world, EntityType<T> type, double x, double y, double z) {
		T entityLiving = type.create(world);
		if (entityLiving == null) {
			return null;
		}
		return spawnEntity(world, entityLiving, x, y, z);
	}

	public static <T extends Mob> T spawnEntity(Level world, T living, double x, double y, double z) {
		living.moveTo(x, y, z, Mth.wrapDegrees(world.random.nextFloat() * 360.0f), 0.0f);
		living.yHeadRot = living.getYRot();
		living.yBodyRot = living.getYRot();
		DifficultyInstance diff = world.getCurrentDifficultyAt(BlockPos.containing(x, y, z));
		//TODO - check SpawnReason
		living.finalizeSpawn((ServerLevel) world, diff, MobSpawnType.MOB_SUMMONED, null, null);
		world.addFreshEntity(living);
		//TODO - right sound?
		living.playAmbientSound();
		return living;
	}
}
