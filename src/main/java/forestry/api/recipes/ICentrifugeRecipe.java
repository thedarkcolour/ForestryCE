package forestry.api.recipes;

import forestry.api.core.Product;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface ICentrifugeRecipe extends IForestryRecipe {
	/**
	 * The item for this recipe to match against.
	 **/
	Ingredient getInput();

	/**
	 * The time it takes to process one item. Default is 20.
	 **/
	int getProcessingTime();

	/**
	 * Returns the randomized products from processing one input item.
	 **/
	List<ItemStack> getProducts(RandomSource random, double outputMult);

	/**
	 * @deprecated Use {@link #getProducts(RandomSource, double)}
	 */
	@Deprecated
	default List<ItemStack> getProducts(RandomSource random) {
		return getProducts(random, 1.0);
	}

	/**
	 * Returns a list of all possible products and their estimated probabilities (0.0 to 1.0],
	 * to help mods that display recipes
	 **/
	List<Product> getAllProducts();
}
