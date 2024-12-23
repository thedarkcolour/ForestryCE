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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.genetics.EffectData;
import forestry.core.utils.VecUtil;

public abstract class ThrottledBeeEffect extends DummyBeeEffect implements IBeeEffect {
	private final boolean isCombinable;
	private final int throttle;
	private final boolean requiresWorkingQueen;

	protected ThrottledBeeEffect(boolean dominant, int throttle, boolean requiresWorking, boolean isCombinable) {
		super(dominant);
		this.throttle = throttle;
		this.isCombinable = isCombinable;
		this.requiresWorkingQueen = requiresWorking;
	}

	public static AABB getBounding(IBeeHousing housing, IGenome genome) {
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
		Vec3i territory = Bee.getAdjustedTerritory(genome, beeModifier);

		Vec3i offset = VecUtil.scale(territory, -1 / 2.0f);

		BlockPos min = housing.getCoordinates().offset(offset);
		BlockPos max = min.offset(territory);

		return new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
	}

	public static <T extends Entity> List<T> getEntitiesInRange(IGenome genome, IBeeHousing housing, Class<T> entityClass) {
		AABB boundingBox = getBounding(housing, genome);
		return housing.getWorldObj().getEntitiesOfClass(entityClass, boundingBox);
	}

	@Override
	public boolean isCombinable() {
		return this.isCombinable;
	}

	@Override
	public IEffectData validateStorage(IEffectData storedData) {
		if (storedData instanceof EffectData) {
			return storedData;
		}

		return new EffectData(1, 0);
	}

	@Override
	public final IEffectData doEffect(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		if (isThrottled(storedData, housing)) {
			return storedData;
		}
		return doEffectThrottled(genome, storedData, housing);
	}

	private boolean isThrottled(IEffectData storedData, IBeeHousing housing) {
		if (this.requiresWorkingQueen && housing.getErrorLogic().hasErrors()) {
			return true;
		}

		int time = storedData.getInteger(0);
		time++;
		storedData.setInteger(0, time);

		if (time < throttle) {
			return true;
		}

		// Reset since we are done throttling.
		storedData.setInteger(0, 0);
		return false;
	}

	abstract IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing);
}
