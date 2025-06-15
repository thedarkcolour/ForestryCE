package forestry.api.core;

import forestry.api.genetics.IEffectData;

/**
 * Basic effect allele.
 */
public interface IEffect {
	/**
	 * @return Whether this effect can work with another combinable effect on the same chromosome pair.
	 */
	boolean isCombinable();

	/**
	 * Returns the passed data storage if it is valid for this effect or a new one if the passed storage object was invalid for this effect.
	 *
	 * @return {@link IEffectData} for the next cycle.
	 */
	IEffectData validateStorage(IEffectData storedData);
}
