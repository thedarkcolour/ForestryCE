package forestry.lepidopterology.entities;

import java.util.EnumSet;

public class AIButterflyWander extends AIButterflyMovement {
	public AIButterflyWander(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.entity.getDestination() != null) {
			return false;
		}

        this.flightTarget = getRandomDestination();
		if (this.flightTarget == null) {
			if (this.entity.getState().doesMovement) {
                this.entity.setState(EnumButterflyState.HOVER);
			}
			return false;
		}

        this.entity.setDestination(this.flightTarget);
        this.entity.setState(EnumButterflyState.FLYING);
		return true;
	}
}
