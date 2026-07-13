package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A fuel usable by the Peat-fired (Copper) Engine. Defined by datapack recipes of type {@code forestry:peat_fuel},
 * so packs and addons can add, remove, or retune peat fuels without code.
 */
public interface IPeatFuel extends IForestryRecipe {
	/**
	 * @return The item(s) accepted as fuel.
	 */
	Ingredient getInput();

	/**
	 * @return Power (RF/energy) produced by this fuel per work cycle.
	 */
	int getPowerPerCycle();

	/**
	 * @return Amount of work cycles a single item of this fuel lasts before being consumed.
	 */
	int getBurnDuration();

	default boolean matches(ItemStack stack) {
		return getInput().test(stack);
	}
}
