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
package forestry.apiculture.genetics.effects;

import java.util.List;

import net.minecraft.world.entity.monster.Monster;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.core.damage.CoreDamageTypes;

public class HeroicBeeEffect extends ThrottledBeeEffect {
	public HeroicBeeEffect() {
		super(false, 40, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<Monster> mobs = ThrottledBeeEffect.getEntitiesInRange(genome, housing, Monster.class);
		for (Monster mob : mobs) {
			mob.hurt(CoreDamageTypes.source(housing.getWorldObj(), CoreDamageTypes.HEROIC), 2);
		}

		return storedData;
	}
}
