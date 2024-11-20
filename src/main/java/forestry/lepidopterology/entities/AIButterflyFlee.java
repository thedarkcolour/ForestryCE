/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.lepidopterology.entities;

import java.util.EnumSet;

import net.minecraft.world.entity.player.Player;

import forestry.api.genetics.alleles.ButterflyChromosomes;

public class AIButterflyFlee extends AIButterflyMovement {
	public AIButterflyFlee(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		Player player = entity.level().getNearestPlayer(entity, entity.getButterfly().getGenome().getActiveValue(ButterflyChromosomes.SPECIES).getFlightDistance());

		if (player == null || player.isShiftKeyDown()) {
			return false;
		}

		if (!entity.getSensing().hasLineOfSight(player)) {
			return false;
		}

		flightTarget = getRandomDestination();
		if (flightTarget == null) {
			return false;
		}

		if (player.distanceToSqr(flightTarget.x, flightTarget.y, flightTarget.z) < player.distanceTo(entity)) {
			return false;
		}

		entity.setDestination(flightTarget);
		entity.setState(EnumButterflyState.FLYING);
		return true;
	}

}
