/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.genetics;

import net.minecraft.world.level.Level;

/**
 * An individual with health and a lifespan.
 */
public interface IIndividualLiving extends IIndividual {
	/**
	 * @return Current health of the individual.
	 */
	int getHealth();

	/**
	 * Set the current health of the individual.
	 */
	void setHealth(int health);

	/**
	 * @return Maximum health of the individual.
	 */
	int getMaxHealth();

	/**
	 * Age the individual.
	 *
	 * @param level   The world where the individual lives.
	 * @param ageStep The amount to age this by. Base amount is {@code 1f}, and higher values should age faster.
	 *                {@code 0f} should not age, and negative values should instantly kill this individual.
	 */
	void age(Level level, float ageStep);

	/**
	 * @return true if the individual is among the living.
	 */
	boolean isAlive();

	@Override
	IIndividualLiving copy();
}
