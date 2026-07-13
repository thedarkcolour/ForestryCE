package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A substrate that activates the rainmaker. Defined by datapack recipes of type {@code forestry:rain_substrate},
 * so packs and addons can add, remove, or retune substrates without code.
 */
public interface IRainSubstrate extends IForestryRecipe {
	/**
	 * @return The item(s) accepted as a substrate.
	 */
	Ingredient getInput();

	/**
	 * @return Duration of the rain shower triggered by this substrate, in Minecraft ticks.
	 */
	int duration();

	/**
	 * @return Speed of the activation sequence triggered.
	 */
	float speed();

	/**
	 * @return Whether the substrate stops rain instead of starting it.
	 */
	boolean reverse();

	default boolean matches(ItemStack stack) {
		return getInput().test(stack);
	}
}
