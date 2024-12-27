/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.apiculture;

import javax.annotation.Nullable;

import net.minecraft.core.Vec3i;

import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.alleles.BeeChromosomes;

/**
 * A bee modifier allows modifying certain conditions in a bee hive.
 */
public interface IBeeModifier {
	/**
	 * Used to modify the territory of a bee, which is the area in which bees can find flowers,
	 * affect mobs with their IBeeEffect, and pollinate tree leaves.
	 *
	 * @param genome          Genome of the bee this modifier is called for.
	 * @param currentModifier Current modifier. Starts out as the value of the allele
	 * @return Float used to modify the base territory.
	 */
	default Vec3i modifyTerritory(IGenome genome, Vec3i currentModifier) {
		return currentModifier;
	}

	/**
	 * Used to modify the chance for mutations to happen.
	 *
	 * @param genome        Genome of the bee this modifier is called for.
	 * @param mate          Genome of the bee mate this modifier is called for.
	 * @param mutation      The mutation that might occur.
	 * @param currentChance The current mutation chance. Starts at the base chance of the mutation but
	 *                      may have already been modified by other IBeeModifiers. Between 0f and 100f.
	 * @return Float used to modify the base mutation chance.
	 */
	default float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
		return currentChance;
	}

	/**
	 * Used to speed up or slow down the aging of a queen.
	 *
	 * @param genome       Genome of the bee this modifier is called for.
	 * @param mate         Genome of the mate, {@code null} if there this bee is not mated.
	 * @param currentAging Current amount to age the bee by. Starts at {@code 1} but may have already been
	 *                     modified by other IBeeModifiers.
	 * @return The number of age steps to age. This number will be rounded if it is not whole. Default is {@code 1f}.
	 * To cancel aging, return {@code 0}. To instantly kill the bee, return a negative value.
	 */
	default float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
		return currentAging;
	}

	/**
	 * Used to increase or decrease the chances of producing products and specialties.
	 *
	 * @param genome       Genome of the bee this modifier is called for.
	 * @param currentSpeed Current production speed. Starts at the value of the bee's active
	 *                     {@link BeeChromosomes#SPEED} allele.
	 * @return Float determining the production speed of queens.
	 */
	default float modifyProductionSpeed(IGenome genome, float currentSpeed) {
		return currentSpeed;
	}

	/**
	 * @param genome             Genome of the bee this modifier is called for.
	 * @param currentPollination Current pollination. Starts out at the value of the bee's active
	 *                           {@link BeeChromosomes#POLLINATION} allele.
	 * @return The chance between 0 and 100 (inclusive) for a flower to be placed and for pollen to be collected/transferred.
	 */
	default float modifyPollination(IGenome genome, float currentPollination) {
		return currentPollination;
	}

	/**
	 * @param genome       Genome of the bee this modifier is called for.
	 * @param currentDecay Current decay. Starts out at {@code 1f} but may already have been modified by other modifiers.
	 * @return The new decay chance for a swarmer queen to die off.
	 */
	default float modifyGeneticDecay(IGenome genome, float currentDecay) {
		return currentDecay;
	}

	/**
	 * @return Whether bees in this housing can fly in the rain without {@link BeeChromosomes#TOLERATES_RAIN}.
	 */
	default boolean isSealed() {
		return false;
	}

	/**
	 * @param genome Genome of the bee this modifier is called for. Can be used to query the activity type.
	 * @return Whether the bees in this hive should be active regardless of their activity type.
	 */
	default boolean isAlwaysActive(IGenome genome) {
		return false;
	}

	/**
	 * @return Whether bees in the hive can work when the sky is obstructed without {@link BeeChromosomes#CAVE_DWELLING}.
	 */
	default boolean isSunlightSimulated() {
		return false;
	}

	/**
	 * @return Whether this hive simulates a {@link TemperatureType#HELLISH hellish} climate even if not in the Nether.
	 */
	default boolean isHellish() {
		return false;
	}
}
