package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A resource consumed by the moistener as it decays (e.g. wheat -> mouldy wheat -> decaying wheat -> mulch).
 * Defined by datapack recipes of type {@code forestry:moistener_fuel}, so packs and addons can add, remove, or
 * retune the decay chain without code. This is distinct from {@link IMoistenerRecipe}, which is the working-slot
 * product recipe (e.g. wheat seeds -> mycelium).
 */
public interface IMoistenerFuel extends IForestryRecipe {
	/**
	 * @return The item(s) accepted as a resource.
	 */
	Ingredient getInput();

	/**
	 * @return The item this resource decays into (i.e. mouldy wheat, decaying wheat, mulch).
	 */
	ItemStack product();

	/**
	 * @return Ordering stage. Resources with a lower stage value are consumed first.
	 */
	int stage();

	/**
	 * @return How many moistener ticks this resource contributes before decaying into its product.
	 */
	int moistenerValue();

	default boolean matches(ItemStack stack) {
		return getInput().test(stack);
	}
}
