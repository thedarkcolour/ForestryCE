package forestry.factory.recipes;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import forestry.api.recipes.IFermenterRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FermenterRecipe implements IFermenterRecipe {
	private final ResourceLocation id;
	private final Ingredient resource;
	private final int fermentationValue;
	private final float modifier;
	private final Fluid output;
	private final FluidStack fluidResource;

	public FermenterRecipe(ResourceLocation id, Ingredient resource, int fermentationValue, float modifier, Fluid output, FluidStack fluidResource) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(resource, "Fermenter Resource cannot be null!");
		Preconditions.checkArgument(!resource.isEmpty(), "Fermenter Resource item cannot be empty!");
		Preconditions.checkNotNull(output, "Fermenter Output cannot be null!");
		Preconditions.checkNotNull(fluidResource, "Fermenter Liquid cannot be null!");

		this.id = id;
		this.resource = resource;
		this.fermentationValue = fermentationValue;
		this.modifier = modifier;
		this.output = output;
		this.fluidResource = fluidResource;
	}

	public FermenterRecipe(ResourceLocation id, int fermentationValue, float modifier, Fluid output, FluidStack fluidResource) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(output, "Fermenter output cannot be null!");
		Preconditions.checkNotNull(fluidResource, "Fermenter liquid cannot be null!");

		this.id = id;
		this.resource = Ingredient.EMPTY;
		this.fermentationValue = fermentationValue;
		this.modifier = modifier;
		this.output = output;
		this.fluidResource = fluidResource;
	}

	@Override
	public Ingredient getInputItem() {
		return this.resource;
	}

	@Override
	public FluidStack getInputFluid() {
		return this.fluidResource;
	}

	@Override
	public int getFermentationValue() {
		return this.fermentationValue;
	}

	@Override
	public float getModifier() {
		return this.modifier;
	}

	@Override
	public Fluid getOutput() {
		return this.output;
	}

	@Override
	public boolean matches(ItemStack inputItem, FluidStack inputFluid) {
		return this.resource.test(inputItem) && this.fluidResource.isFluidEqual(inputFluid);
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.FERMENTER.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.FERMENTER.type();
	}

	public static class Serializer implements RecipeSerializer<FermenterRecipe> {
		@Override
		public FermenterRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient resource = RecipeSerializers.deserialize(json.get("resource"));
			int fermentationValue = GsonHelper.getAsInt(json, "fermentationValue");
			float modifier = GsonHelper.getAsFloat(json, "modifier");
			Fluid output = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "output")));
			FluidStack fluidResource = RecipeSerializers.deserializeFluid(GsonHelper.getAsJsonObject(json, "fluidResource"));

			return new FermenterRecipe(recipeId, resource, fermentationValue, modifier, output, fluidResource);
		}

		@Override
		public FermenterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient resource = Ingredient.fromNetwork(buffer);
			int fermentationValue = buffer.readVarInt();
			float modifier = buffer.readFloat();
			Fluid output = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			FluidStack fluidResource = FluidStack.readFromPacket(buffer);

			return new FermenterRecipe(recipeId, resource, fermentationValue, modifier, output, fluidResource);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FermenterRecipe recipe) {
			recipe.resource.toNetwork(buffer);
			buffer.writeVarInt(recipe.fermentationValue);
			buffer.writeFloat(recipe.modifier);
			buffer.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(recipe.output));
			recipe.fluidResource.writeToPacket(buffer);
		}
	}
}
