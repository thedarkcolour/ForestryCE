package forestry.api.recipes;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IStillRecipe extends IForestryRecipe {
	/**
	 * @return Amount of work cycles required to run through the conversion once.
	 */
	int getCyclesPerUnit();

	/**
	 * @return FluidStack representing the input liquid.
	 */
	FluidStack getInput();

	/**
	 * @return FluidStack representing the output liquid.
	 */
	FluidStack getOutput();

	boolean matches(FluidStack input);
}
