package forestry.core.data.builder;

import com.google.gson.JsonObject;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.RecipeSerializers;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public class FabricatorSmeltingRecipeBuilder {

	private int meltingPoint;
	private Ingredient resource;
	private FluidStack product;

	public FabricatorSmeltingRecipeBuilder setMeltingPoint(int meltingPoint) {
		this.meltingPoint = meltingPoint;
		return this;
	}

	public FabricatorSmeltingRecipeBuilder setResource(Ingredient resource) {
		this.resource = resource;
		return this;
	}

	public FabricatorSmeltingRecipeBuilder setProduct(FluidStack product) {
		this.product = product;
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		consumer.accept(new Result(id, this.meltingPoint, this.resource, this.product));
	}

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final int meltingPoint;
		private final Ingredient resource;
		private final FluidStack product;

		public Result(ResourceLocation id, int meltingPoint, Ingredient resource, FluidStack product) {
			this.id = id;
			this.meltingPoint = meltingPoint;
			this.resource = resource;
			this.product = product;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty("melting", this.meltingPoint);
			json.add("resource", this.resource.toJson());
			json.add("product", RecipeSerializers.serializeFluid(this.product));
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return FactoryRecipeTypes.FABRICATOR_SMELTING.serializer();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}
}
