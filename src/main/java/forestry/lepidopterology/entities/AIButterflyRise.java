package forestry.lepidopterology.entities;

import java.util.EnumSet;

public class AIButterflyRise extends AIButterflyMovement {
	public AIButterflyRise(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.entity.getDestination() != null) {
			return false;
		}

		if (!this.entity.horizontalCollision && this.entity.getRandom().nextInt(64) != 0) {
			return false;
		}

        this.flightTarget = getRandomDestinationUpwards();
		if (this.flightTarget == null) {
			if (this.entity.getState().doesMovement) {
                this.entity.setState(EnumButterflyState.HOVER);
			}
			return false;
		}

        this.entity.setDestination(this.flightTarget);
        this.entity.setState(EnumButterflyState.RISING);
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (this.entity.getState() != EnumButterflyState.RISING) {
			return false;
		}
		if (this.flightTarget == null) {
			return false;
		}
		// Abort if the flight target changed on us.
		if (this.entity.getDestination() == null || !this.entity.getDestination().equals(this.flightTarget)) {
			return false;
		}

		// Continue if we have not yet reached the destination.
		if (this.entity.getDestination().distanceToSqr(this.entity.position()) > 2.0f) {
			return true;
		}

        this.entity.setDestination(null);
		return false;
	}

	@Override
	public void tick() {
		if (this.entity.isInWater()) {
            this.flightTarget = getRandomDestinationUpwards();
		} else if (this.entity.verticalCollision && this.entity.getRandom().nextInt(62) == 0) {
            this.flightTarget = null;
		}

        this.entity.setDestination(this.flightTarget);
		if (this.flightTarget != null) {
            this.entity.getNavigation().moveTo(this.flightTarget.x, this.flightTarget.y, this.flightTarget.z, 0.5f);
		}
        this.entity.changeExhaustion(1);
	}
}
