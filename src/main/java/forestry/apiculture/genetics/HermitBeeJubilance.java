package forestry.apiculture.genetics;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.effects.ThrottledBeeEffect;
import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * Hermits will not produce if there are any other living creatures nearby.
 */
public enum HermitBeeJubilance implements IBeeJubilance {
	INSTANCE;

	@Override
	public boolean isJubilant(IBeeSpecies species, IGenome genome, IBeeHousing housing) {
		List<Mob> list = ThrottledBeeEffect.getEntitiesInRange(genome, housing, Mob.class);
		return list.isEmpty();
	}
}
