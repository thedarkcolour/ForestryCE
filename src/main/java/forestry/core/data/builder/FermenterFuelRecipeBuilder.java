package forestry.core.data.builder;

import forestry.factory.recipes.FermenterFuelRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class FermenterFuelRecipeBuilder {
	private Ingredient fuel;
	private int fermentPerCycle;
	private int burnDuration;

	public FermenterFuelRecipeBuilder setFuel(Ingredient fuel) {
		this.fuel = fuel;
		return this;
	}

	public FermenterFuelRecipeBuilder setFermentPerCycle(int fermentPerCycle) {
		this.fermentPerCycle = fermentPerCycle;
		return this;
	}

	public FermenterFuelRecipeBuilder setBurnDuration(int burnDuration) {
		this.burnDuration = burnDuration;
		return this;
	}

	public void build(RecipeOutput output, ResourceLocation id) {
		output.accept(id, new FermenterFuelRecipe(id, this.fuel, this.fermentPerCycle, this.burnDuration), null);
	}
}
