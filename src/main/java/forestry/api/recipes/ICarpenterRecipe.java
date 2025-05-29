package forestry.api.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public interface ICarpenterRecipe extends IForestryRecipe {
	/**
	 * @return Number of work cycles required to craft the recipe once.
	 */
	int getPackagingTime();

	/**
	 * @return the crafting grid recipe. The crafting recipe's getRecipeOutput() is used as the ICarpenterRecipe's output.
	 */
	CraftingRecipe getCraftingGridRecipe();

	/**
	 * @return the box required for this recipe. return empty stack if there is no required box.
	 * Examples of boxes are the Forestry cartons and crates.
	 */
	Ingredient getBox();

	/**
	 * @return the fluid required for this recipe. return {@link FluidStack#EMPTY} if there is no required fluid.
	 */
	FluidStack getInputFluid();

	boolean matches(FluidStack fluid, ItemStack boxStack, Container craftingInventory, Level level);
}
