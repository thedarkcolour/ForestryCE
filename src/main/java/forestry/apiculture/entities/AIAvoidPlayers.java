package forestry.apiculture.entities;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class AIAvoidPlayers extends Goal {
	private final PathfinderMob mob;
	private final PathNavigation pathNavigator;

	private final float farSpeed;
	private final float nearSpeed;
	private final float minDistance;

	@Nullable
	private Path path;

	@Nullable
	private Player player;

	public AIAvoidPlayers(PathfinderMob mob, float minDistance, float farSpeed, float nearSpeed) {
		this.mob = mob;
		this.minDistance = minDistance;
		this.farSpeed = farSpeed;
		this.nearSpeed = nearSpeed;
		this.pathNavigator = mob.getNavigation();
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
        this.player = this.mob.level().getNearestPlayer(this.mob, this.minDistance);

		if (this.player == null) {
			return false;
		}

		if (!this.mob.getSensing().hasLineOfSight(this.player)) {
			return false;
		}

		Vec3 randomTarget = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.player.position());

		if (randomTarget == null) {
			return false;
		}

		if (this.player.distanceToSqr(randomTarget.x, randomTarget.y, randomTarget.z) < this.player.distanceTo(this.mob)) {
			return false;
		}

        this.path = this.pathNavigator.createPath(randomTarget.x, randomTarget.y, randomTarget.z, 0);
		return this.path != null;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.pathNavigator.isDone();
	}

	@Override
	public void start() {
		this.pathNavigator.moveTo(this.path, this.farSpeed);
	}

	@Override
	public void stop() {
        this.player = null;
	}

	@Override
	public void tick() {
		if (this.player != null && this.mob.distanceTo(this.player) < 49.0D) {
            this.mob.getNavigation().setSpeedModifier(this.nearSpeed);
		} else {
            this.mob.getNavigation().setSpeedModifier(this.farSpeed);
		}
	}
}
