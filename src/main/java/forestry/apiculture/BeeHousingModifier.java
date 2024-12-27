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
package forestry.apiculture;

import javax.annotation.Nullable;

import net.minecraft.core.Vec3i;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;

public class BeeHousingModifier implements IBeeModifier {
	private final IBeeHousing beeHousing;

	public BeeHousingModifier(IBeeHousing beeHousing) {
		this.beeHousing = beeHousing;
	}

	@Override
	public Vec3i modifyTerritory(IGenome genome, Vec3i currentModifier) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			currentModifier = modifier.modifyTerritory(genome, currentModifier);
		}
		return currentModifier;
	}

	@Override
	public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			currentChance = modifier.modifyMutationChance(genome, mate, mutation, currentChance);
		}
		return currentChance;
	}

	@Override
	public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			currentAging = modifier.modifyAging(genome, mate, currentAging);
		}
		return currentAging;
	}

	@Override
	public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			currentSpeed = modifier.modifyProductionSpeed(genome, currentSpeed);
		}
		return currentSpeed;
	}

	@Override
	public float modifyPollination(IGenome genome, float currentPollination) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			currentPollination = modifier.modifyPollination(genome, currentPollination);
		}
		return currentPollination;
	}

	@Override
	public float modifyGeneticDecay(IGenome genome, float currentDecay) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			currentDecay = modifier.modifyGeneticDecay(genome, currentDecay);
		}
		return currentDecay;
	}

	@Override
	public boolean isSealed() {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			if (modifier.isSealed()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAlwaysActive(IGenome genome) {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			if (modifier.isAlwaysActive(genome)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSunlightSimulated() {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			if (modifier.isSunlightSimulated()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isHellish() {
		for (IBeeModifier modifier : beeHousing.getBeeModifiers()) {
			if (modifier.isHellish()) {
				return true;
			}
		}
		return false;
	}
}
