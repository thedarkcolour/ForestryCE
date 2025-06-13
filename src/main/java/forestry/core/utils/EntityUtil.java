package forestry.core.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;

public abstract class EntityUtil {
	@Nullable
	public static <T extends Mob> T spawnEntity(Level level, EntityType<T> type, double x, double y, double z) {
		T entityLiving = type.create(level);
		if (entityLiving == null) {
			return null;
		}
		return spawnEntity(level, entityLiving, x, y, z);
	}

	public static <T extends Mob> T spawnEntity(Level level, T living, double x, double y, double z) {
		living.moveTo(x, y, z, level.random.nextFloat() * 360.0f, 0.0f);
		living.yHeadRot = living.getYRot();
		living.yBodyRot = living.getYRot();
		DifficultyInstance diff = level.getCurrentDifficultyAt(BlockPos.containing(x, y, z));
		EventHooks.finalizeMobSpawn(living, (ServerLevel) level, diff, MobSpawnType.MOB_SUMMONED, null);
		level.addFreshEntity(living);
		living.playAmbientSound();
		return living;
	}
}
