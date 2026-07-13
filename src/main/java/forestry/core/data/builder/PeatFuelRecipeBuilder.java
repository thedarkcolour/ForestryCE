package forestry.core.data.builder;

import forestry.energy.recipes.PeatFuelRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class PeatFuelRecipeBuilder {
	private Ingredient fuel;
	private int powerPerCycle;
	private int burnDuration;

	public PeatFuelRecipeBuilder setFuel(Ingredient fuel) {
		this.fuel = fuel;
		return this;
	}

	public PeatFuelRecipeBuilder setPowerPerCycle(int powerPerCycle) {
		this.powerPerCycle = powerPerCycle;
		return this;
	}

	public PeatFuelRecipeBuilder setBurnDuration(int burnDuration) {
		this.burnDuration = burnDuration;
		return this;
	}

	public void build(RecipeOutput output, ResourceLocation id) {
		output.accept(id, new PeatFuelRecipe(id, this.fuel, this.powerPerCycle, this.burnDuration), null);
	}
}
