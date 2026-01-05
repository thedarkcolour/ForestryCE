package forestry.core.data.builder;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import forestry.api.core.Product;
import forestry.core.utils.JsonUtil;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CentrifugeRecipeBuilder {
	private int processingTime;
	private Ingredient input;
	private final ArrayList<Product> outputs = new ArrayList<>();

	public CentrifugeRecipeBuilder setProcessingTime(int processingTime) {
		this.processingTime = processingTime;
		return this;
	}

	public CentrifugeRecipeBuilder setInput(Ingredient input) {
		this.input = input;
		return this;
	}

	public CentrifugeRecipeBuilder product(float chance, ItemStack stack) {
        this.outputs.add(new Product(stack.getItem(), stack.getCount(), stack.getTag(), chance));
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		Preconditions.checkState(!this.outputs.isEmpty(), "Empty centrifuge recipes are not allowed");
		consumer.accept(new Result(id, this.processingTime, this.input, this.outputs));
	}

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final int processingTime;
		private final Ingredient input;
		private final ArrayList<Product> outputs;

		public Result(ResourceLocation id, int processingTime, Ingredient input, ArrayList<Product> outputs) {
			this.id = id;
			this.processingTime = processingTime;
			this.input = input;
			this.outputs = outputs;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty("time", this.processingTime);
			json.add("input", this.input.toJson());

			JsonArray products = new JsonArray();

			for (Product product : this.outputs) {
				products.add(JsonUtil.serialize(Product.CODEC, product));
			}

			json.add("products", products);
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return FactoryRecipeTypes.CENTRIFUGE.serializer();
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
