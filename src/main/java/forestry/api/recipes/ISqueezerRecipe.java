package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public interface ISqueezerRecipe extends IForestryRecipe {
	/**
	 * @return item stacks representing the required resources for one process. Stack size will be taken into account.
	 */
	List<Ingredient> getInputs();

	/**
	 * @return Number of work cycles required to squeeze one set of resources.
	 */
	int getProcessingTime();

	/**
	 * @return Item stack representing the possible remnants from this recipe. (i.e. tin left over from tin cans)
	 */
	ItemStack getRemnants();

	/**
	 * @return Chance remnants will be produced by a single recipe cycle, from 0 to 1.
	 */
	float getRemnantsChance();

	/**
	 * @return {@link FluidStack} representing the output of this recipe.
	 */
	FluidStack getFluidOutput();
}
