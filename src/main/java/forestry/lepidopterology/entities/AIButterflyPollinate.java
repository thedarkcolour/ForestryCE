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

import forestry.api.IForestryApi;
import forestry.api.genetics.pollen.IPollen;
import forestry.api.genetics.pollen.IPollenManager;

public class AIButterflyPollinate extends AIButterflyInteract {
	public AIButterflyPollinate(EntityButterfly entity) {
		super(entity);
	}

	@Override
	protected boolean canInteract() {
		return this.rest != null && IForestryApi.INSTANCE.getPollenManager().canPollinate(entity.level(), rest, entity.getButterfly());
	}

	@Override
	public void tick() {
		if (canContinueToUse() && rest != null) {
			IPollenManager pollens = IForestryApi.INSTANCE.getPollenManager();
			IPollen<?> butterflyPollen = entity.getPollen();

			if (butterflyPollen == null) {
				entity.setPollen(pollens.getPollen(entity.level(), rest, entity.getButterfly()));
				entity.changeExhaustion(-entity.getExhaustion());
			} else if (butterflyPollen.tryPollinate(entity.level(), rest, entity.getPollen())) {
				entity.setPollen(null);
			}
			setHasInteracted();
			entity.cooldownPollination = EntityButterfly.COOLDOWNS;
		}
	}

}
