package forestry.core.data.builder;

import forestry.energy.recipes.BiogasFuelRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class BiogasFuelRecipeBuilder {
	private FluidIngredient fluid;
	private int powerPerCycle;
	private int burnDuration;
	private int dissipationMultiplier = 1;

	public BiogasFuelRecipeBuilder setFluid(FluidIngredient fluid) {
		this.fluid = fluid;
		return this;
	}

	public BiogasFuelRecipeBuilder setPowerPerCycle(int powerPerCycle) {
		this.powerPerCycle = powerPerCycle;
		return this;
	}

	public BiogasFuelRecipeBuilder setBurnDuration(int burnDuration) {
		this.burnDuration = burnDuration;
		return this;
	}

	public BiogasFuelRecipeBuilder setDissipationMultiplier(int dissipationMultiplier) {
		this.dissipationMultiplier = dissipationMultiplier;
		return this;
	}

	public void build(RecipeOutput output, ResourceLocation id) {
		output.accept(id, new BiogasFuelRecipe(id, this.fluid, this.powerPerCycle, this.burnDuration, this.dissipationMultiplier), null);
	}
}
