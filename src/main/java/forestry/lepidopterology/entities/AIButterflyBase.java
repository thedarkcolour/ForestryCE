package forestry.lepidopterology.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class AIButterflyBase extends Goal {

	protected final EntityButterfly entity;

	protected AIButterflyBase(EntityButterfly entity) {
		this.entity = entity;
	}

	@Nullable
	protected Vec3 getRandomDestination() {
		if (this.entity.isInWater()) {
			return getRandomDestinationUpwards();
		}

		Vec3 entityPos = this.entity.position();
		Vec3 randomTarget = DefaultRandomPos.getPosAway(this.entity, 16, 7, entityPos.add(new Vec3(0, -1, 1).yRot(this.entity.getYRot())));

		if (randomTarget != null && validateDestination(randomTarget, false)) {
			return randomTarget;
		}
		return null;
	}

	@Nullable
	protected Vec3 getRandomDestinationUpwards() {
		Vec3 entityPos = this.entity.position();
		Vec3 destination = entityPos.add(0, this.entity.getRandom().nextInt(10) + 2, 0);
		if (validateDestination(destination, true)) {
			return destination;
		} else {
			return null;
		}
	}

	private boolean validateDestination(Vec3 dest, boolean allowFluids) {
		if (dest.y < 1) {
			return false;
		}
		BlockPos pos = BlockPos.containing(dest);
		Level level = this.entity.level();
		if (!level.hasChunkAt(pos)) {
			return false;
		}
		BlockState blockState = level.getBlockState(pos);
		if (!allowFluids && blockState.liquid()) {
			return false;
		}
		//		if (!block.isPassable(entity.world, pos)) {
		if (!blockState.isAir()) {    //TODO
			return false;
		}
		return this.entity.getButterfly().isAcceptedEnvironment(level, dest.x, dest.y, dest.z);
	}

}
