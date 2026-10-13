package forestry.api.apiculture.genetics;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

/**
 * Creates the bee effects that Forestry can load from a datapack.
 *
 * @see IBeeSpeciesType#getEffectFactory
 */
public interface IBeeEffectFactory {
	/**
	 * Creates a bee effect that applies a mob effect to nearby entities every 200 ticks.
	 *
	 * @param dominant The dominance of the bee effect allele
	 * @param effect   The mob effect to apply
	 * @param duration The duration of the mob effect in ticks
	 * @return The bee effect, which has the {@code forestry:apply_potion} type
	 */
	IBeeEffect createPotion(boolean dominant, Holder<MobEffect> effect, int duration);

	/**
	 * Creates a bee effect that applies a mob effect to nearby entities.
	 *
	 * @param dominant The dominance of the bee effect allele
	 * @param effect   The mob effect to apply
	 * @param duration The duration of the mob effect in ticks
	 * @param throttle The number of ticks between applications
	 * @param chance   The chance from 0 to 1 that an entity receives the mob effect
	 * @return The bee effect, which has the {@code forestry:apply_potion} type
	 */
	IBeeEffect createPotion(boolean dominant, Holder<MobEffect> effect, int duration, int throttle, float chance);
}
