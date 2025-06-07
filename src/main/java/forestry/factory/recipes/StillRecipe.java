package forestry.factory.recipes;

import com.google.gson.JsonObject;
import forestry.api.recipes.IStillRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class StillRecipe implements IStillRecipe {
	private final int timePerUnit;
	private final SizedFluidIngredient input;
	private final FluidStack output;

	public StillRecipe(int timePerUnit, SizedFluidIngredient input, FluidStack output) {
		this.timePerUnit = timePerUnit;
		this.input = input;
		this.output = output;
	}

	@Override
	public int getCyclesPerUnit() {
		return this.timePerUnit;
	}

	@Override
	public FluidStack getInput() {
		return this.input;
	}

	@Override
	public FluidStack getOutput() {
		return this.output;
	}

	@Override
	public boolean matches(FluidStack input) {
		return input.containsFluid(this.input);
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.STILL.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.STILL.type();
	}

	public static class Serializer implements RecipeSerializer<StillRecipe> {
		@Override
		public StillRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			int timePerUnit = GsonHelper.getAsInt(json, "time");
			FluidStack input = RecipeSerializers.deserializeFluid(GsonHelper.getAsJsonObject(json, "input"));
			FluidStack output = RecipeSerializers.deserializeFluid(GsonHelper.getAsJsonObject(json, "output"));

			return new StillRecipe(recipeId, timePerUnit, input, output);
		}

		@Override
		public StillRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			int timePerUnit = buffer.readVarInt();
			FluidStack input = FluidStack.readFromPacket(buffer);
			FluidStack output = FluidStack.readFromPacket(buffer);

			return new StillRecipe(recipeId, timePerUnit, input, output);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, StillRecipe recipe) {
			buffer.writeVarInt(recipe.timePerUnit);
			recipe.input.writeToPacket(buffer);
			recipe.output.writeToPacket(buffer);
		}
	}
}
