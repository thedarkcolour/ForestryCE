package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A fuel that powers the fermenter (i.e. fertilizer). Defined by datapack recipes of type
 * {@code forestry:fermenter_fuel}, so packs and addons can add, remove, or retune fuels without code.
 */
public interface IFermenterFuel extends IForestryRecipe {
	/**
	 * @return The item(s) accepted as fuel.
	 */
	Ingredient getInput();

	/**
	 * @return How much is fermented per work cycle, i.e. how much biomass is produced per cycle.
	 */
	int fermentPerCycle();

	/**
	 * @return Amount of work cycles a single item of this fuel lasts before expiring.
	 */
	int burnDuration();

	default boolean matches(ItemStack stack) {
		return getInput().test(stack);
	}
}
