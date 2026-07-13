package forestry.core.data.builder;

import forestry.factory.recipes.RainSubstrateRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class RainSubstrateRecipeBuilder {
	private Ingredient substrate;
	private int duration;
	private float speed;
	private boolean reverse;

	public RainSubstrateRecipeBuilder setSubstrate(Ingredient substrate) {
		this.substrate = substrate;
		return this;
	}

	public RainSubstrateRecipeBuilder setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	public RainSubstrateRecipeBuilder setSpeed(float speed) {
		this.speed = speed;
		return this;
	}

	public RainSubstrateRecipeBuilder setReverse(boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public void build(RecipeOutput output, ResourceLocation id) {
		output.accept(id, new RainSubstrateRecipe(id, this.substrate, this.duration, this.speed, this.reverse), null);
	}
}
