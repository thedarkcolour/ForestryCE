package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public interface IFermenterRecipe extends IForestryRecipe {
	/**
	 * @return ItemStack representing the input resource.
	 */
	Ingredient getInputItem();

	/**
	 * @return Fluid representing the input fluid resource.
	 */
	FluidIngredient getInputFluid();

	/**
	 * @return Value of the given resource, i.e. how much needs to be fermented for the output to be deposited into the product tank.
	 */
	int getFermentationValue();

	/**
	 * @return Modifies the amount of liquid output per work cycle.
	 * (water = 1.0f, honey = 1.5f)
	 */
	float getModifier();

	/**
	 * @return Fluid representing output. Amount is determined by fermentationValue * modifier.
	 */
	Fluid getOutputFluid();

	boolean matches(ItemStack inputItem, FluidStack inputFluid);
}
