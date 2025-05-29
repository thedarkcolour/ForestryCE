package forestry.api.storage;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * To make your own backpack, create a backpack definition and register it with
 * {@link IBackpackInterface#registerBackpackDefinition(String, IBackpackDefinition)}.
 */
public interface IBackpackDefinition {

	/**
	 * @return Primary color for the backpack icon.
	 */
	int getPrimaryColour();

	/**
	 * @return Secondary color for backpack icon, normally white.
	 */
	int getSecondaryColour();

	/**
	 * Filters items that can be put into a backpack.
	 * <p>
	 * For Backpack Implementers: you can create a new filter with
	 * {@link IBackpackInterface#createBackpackFilter()} or
	 * {@link IBackpackInterface#createNaturalistBackpackFilter(String)}
	 * or implement your own.
	 *
	 * @return the backpack's item filter.
	 */
	Predicate<ItemStack> getFilter();
}
