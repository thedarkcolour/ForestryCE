package forestry.api.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Optional;

public interface IFabricatorRecipe extends IForestryRecipe {
	/**
	 * @return the molten liquid (and amount) required for this recipe.
	 */
	Optional<SizedFluidIngredient> getRequiredFluid();

	/**
	 * @return the plan for this recipe (the item in the top right slot)
	 */
	Ingredient getPlan();

	/**
	 * @return the crafting grid recipe. The crafting recipe's getRecipeOutput() is used as the IFabricatorRecipe's output.
	 */
	CraftingRecipe getCraftingGridRecipe();

	boolean matches(Level level, FluidStack liquid, ItemStack stack, Container inventory);
}
