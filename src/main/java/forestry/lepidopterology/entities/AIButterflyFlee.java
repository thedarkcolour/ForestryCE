package forestry.lepidopterology.entities;

import forestry.api.genetics.alleles.ButterflyChromosomes;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class AIButterflyFlee extends AIButterflyMovement {
	public AIButterflyFlee(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		Player player = this.entity.level().getNearestPlayer(this.entity, this.entity.getButterfly().getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getFlightDistance());

		if (player == null || player.isShiftKeyDown()) {
			return false;
		}

		if (!this.entity.getSensing().hasLineOfSight(player)) {
			return false;
		}

        this.flightTarget = getRandomDestination();
		if (this.flightTarget == null) {
			return false;
		}

		if (player.distanceToSqr(this.flightTarget.x, this.flightTarget.y, this.flightTarget.z) < player.distanceTo(this.entity)) {
			return false;
		}

        this.entity.setDestination(this.flightTarget);
        this.entity.setState(EnumButterflyState.FLYING);
		return true;
	}

}
