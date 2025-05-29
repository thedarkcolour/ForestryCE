package forestry.api.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public interface IFabricatorRecipe extends IForestryRecipe {
	/**
	 * @return the molten liquid (and amount) required for this recipe.
	 */
	FluidStack getResultFluid();

	/**
	 * @return the plan for this recipe (the item in the top right slot)
	 */
	Ingredient getPlan();

	/**
	 * @return the crafting grid recipe. The crafting recipe's getRecipeOutput() is used as the IFabricatorRecipe's output.
	 */
	ShapedRecipe getCraftingGridRecipe();

	boolean matches(Level level, FluidStack liquid, ItemStack stack, Container inventory);
}
