package forestry.api.recipes;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

public interface IFabricatorSmeltingRecipe extends IForestryRecipe {
	/**
	 * @return item to be melted down
	 */
	Ingredient getInput();

	/**
	 * @return temperature at which the item melts. Glass is 1000, Sand is 3000.
	 */
	int getMeltingPoint();

	/**
	 * @return resulting fluid
	 */
	FluidStack getResultFluid();
}
