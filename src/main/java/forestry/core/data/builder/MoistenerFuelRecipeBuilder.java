package forestry.core.data.builder;

import forestry.factory.recipes.MoistenerFuelRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class MoistenerFuelRecipeBuilder {
	private Ingredient resource;
	private ItemStack product = ItemStack.EMPTY;
	private int stage;
	private int moistenerValue;

	public MoistenerFuelRecipeBuilder setResource(Ingredient resource) {
		this.resource = resource;
		return this;
	}

	public MoistenerFuelRecipeBuilder setProduct(ItemStack product) {
		this.product = product;
		return this;
	}

	public MoistenerFuelRecipeBuilder setStage(int stage) {
		this.stage = stage;
		return this;
	}

	public MoistenerFuelRecipeBuilder setMoistenerValue(int moistenerValue) {
		this.moistenerValue = moistenerValue;
		return this;
	}

	public void build(RecipeOutput output, ResourceLocation id) {
		output.accept(id, new MoistenerFuelRecipe(id, this.resource, this.product, this.stage, this.moistenerValue), null);
	}
}
