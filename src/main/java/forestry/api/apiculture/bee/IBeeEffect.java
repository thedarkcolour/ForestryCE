package forestry.api.apiculture.bee;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.core.IEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.IRegistryAlleleValue;
import forestry.core.render.ParticleRender;
import net.minecraft.core.BlockPos;

import java.util.List;

public interface IBeeEffect extends IEffect, IRegistryAlleleValue {
	@Override
	default IEffectData validateStorage(IEffectData storedData) {
		return storedData;
	}

	@Override
	default boolean isCombinable() {
		return false;
	}

	/**
	 * Called by apiaries to cause an effect in the world. (server)
	 *
	 * @param genome     Genome of the bee queen causing this effect
	 * @param storedData Object containing the stored effect data for the apiary/hive the bee is in.
	 * @param housing    {@link IBeeHousing} the bee currently resides in.
	 * @return storedData, may have been manipulated.
	 */
	default IEffectData doEffect(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		return storedData;
	}

	/**
	 * Called on the client side to produce visual bee effects.
	 *
	 * @param genome     Genome of the bee queen causing this effect
	 * @param storedData Object containing the stored effect data for the apiary/hive the bee is in.
	 * @param housing    {@link IBeeHousing} the bee currently resides in.
	 * @return storedData, may have been manipulated.
	 */
	default IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		IBeekeepingLogic beekeepingLogic = housing.getBeekeepingLogic();
		List<BlockPos> flowerPositions = beekeepingLogic.getFlowerPositions();

		ParticleRender.addBeeHiveFX(housing, genome, flowerPositions);
		return storedData;
	}
}
