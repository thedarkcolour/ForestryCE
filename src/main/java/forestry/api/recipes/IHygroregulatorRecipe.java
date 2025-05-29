package forestry.api.recipes;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IHygroregulatorRecipe extends IForestryRecipe {
	/**
	 * @return FluidStack containing information on fluid and amount.
	 */
	FluidStack getInputFluid();

	/**
	 * @return How long the temperature change from this recipe will last before more fluid is consumed.
	 */
	int getRetainTime();

	/**
	 * @return The humidity change that this recipe causes in one work cycle.
	 */
	byte getHumiditySteps();

	/**
	 * @return The temperature change that this recipe causes in one work cycle.
	 */
	byte getTemperatureSteps();
}
