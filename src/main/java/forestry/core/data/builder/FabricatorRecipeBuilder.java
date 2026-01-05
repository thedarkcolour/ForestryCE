package forestry.core.data.builder;

import com.google.gson.JsonObject;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.RecipeSerializers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Consumer;

public class FabricatorRecipeBuilder {
	private Ingredient plan;
	private FluidStack molten;
	private ShapedRecipeBuilder.Result recipe;

	public FabricatorRecipeBuilder setPlan(Ingredient plan) {
		this.plan = plan;
		return this;
	}

	public FabricatorRecipeBuilder setMolten(FluidStack molten) {
		this.molten = molten;
		return this;
	}

	public FabricatorRecipeBuilder recipe(ShapedRecipeBuilder recipe) {
		MutableObject<FinishedRecipe> holder = new MutableObject<>();
		recipe.unlockedBy("impossible", new ImpossibleTrigger.TriggerInstance()).save(holder::setValue);
		this.recipe = (ShapedRecipeBuilder.Result) holder.getValue();
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		consumer.accept(new Result(id, this.plan, this.molten, this.recipe));
	}

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final Ingredient plan;
		private final FluidStack molten;
		private final ShapedRecipeBuilder.Result recipe;

		public Result(ResourceLocation id, Ingredient plan, FluidStack molten, ShapedRecipeBuilder.Result recipe) {
			this.id = id;
			this.plan = plan;
			this.molten = molten;
			this.recipe = recipe;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("plan", this.plan.toJson());
			json.add("molten", RecipeSerializers.serializeFluid(this.molten));
			json.add("recipe", this.recipe.serializeRecipe());
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return FactoryRecipeTypes.FABRICATOR.serializer();
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
