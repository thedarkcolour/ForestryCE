package forestry.api.recipes;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

/**
 * A fuel usable by the Biogas (Bronze) Engine. Defined by datapack recipes of type {@code forestry:biogas_fuel},
 * so packs and addons can add, remove, or retune biogas fuels without code.
 */
public interface IBiogasFuel extends IForestryRecipe {
	/**
	 * @return The fluid(s) accepted as fuel. A single bucket is consumed per burn.
	 */
	FluidIngredient getFluidInput();

	/**
	 * @return Power (RF/energy) produced by this fuel per work cycle of the engine.
	 */
	int getPowerPerCycle();

	/**
	 * @return How many work cycles a single bucket of this fuel lasts.
	 */
	int getBurnDuration();

	/**
	 * @return By how much the normal heat dissipation rate of 1 is multiplied when burning this fuel.
	 */
	int getDissipationMultiplier();

	default boolean matches(Fluid fluid) {
		return getFluidInput().test(new FluidStack(fluid, 1));
	}
}
