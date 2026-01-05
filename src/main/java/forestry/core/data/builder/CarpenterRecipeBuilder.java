package forestry.core.data.builder;

import com.google.gson.JsonObject;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.RecipeSerializers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CarpenterRecipeBuilder {
	private int packagingTime = 5;
	@Nullable
	private FluidStack liquid;
	private Ingredient box;
	private FinishedRecipe recipe;
	@Nullable
	private ItemStack result;

	public CarpenterRecipeBuilder setPackagingTime(int packagingTime) {
		this.packagingTime = packagingTime;
		return this;
	}

	public CarpenterRecipeBuilder setLiquid(@Nullable FluidStack liquid) {
		this.liquid = liquid;
		return this;
	}

	public CarpenterRecipeBuilder setBox(Ingredient box) {
		this.box = box;
		return this;
	}

	public CarpenterRecipeBuilder recipe(ShapedRecipeBuilder recipe) {
		MutableObject<FinishedRecipe> holder = new MutableObject<>();
		recipe.unlockedBy("impossible", new ImpossibleTrigger.TriggerInstance()).save(holder::setValue);
		this.recipe = holder.getValue();
		return this;
	}

	public CarpenterRecipeBuilder recipe(ShapelessRecipeBuilder recipe) {
		MutableObject<FinishedRecipe> holder = new MutableObject<>();
		recipe.unlockedBy("impossible", new ImpossibleTrigger.TriggerInstance()).save(holder::setValue);
		this.recipe = holder.getValue();
		return this;
	}

	/**
	 * In case the recipe should create an item stack, and not a basic item without NBT
	 *
	 * @param result The result to override {@link #recipe(ShapedRecipeBuilder)}
	 * @return This builder for chaining
	 */
	public CarpenterRecipeBuilder override(ItemStack result) {
		this.result = result;
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		consumer.accept(new Result(id, this.packagingTime, this.liquid, this.box, this.recipe, this.result));
	}

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final int packagingTime;
		@Nullable
		private final FluidStack liquid;
		private final Ingredient box;
		private final FinishedRecipe recipe;
		@Nullable
		private final ItemStack result;

		public Result(ResourceLocation id, int packagingTime, @Nullable FluidStack liquid, Ingredient box, FinishedRecipe recipe, @Nullable ItemStack result) {
			this.id = id;
			this.packagingTime = packagingTime;
			this.liquid = liquid;
			this.box = box;
			this.recipe = recipe;
			this.result = result;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty("time", this.packagingTime);

			if (this.liquid != null) {
				json.add("liquid", RecipeSerializers.serializeFluid(this.liquid));
			}

			json.add("box", this.box.toJson());
			json.add("recipe", this.recipe.serializeRecipe());

			if (this.result != null) {
				json.add("result", RecipeSerializers.item(this.result));
			}
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return FactoryRecipeTypes.CARPENTER.serializer();
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
