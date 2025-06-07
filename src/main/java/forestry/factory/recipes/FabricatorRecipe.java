package forestry.factory.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.core.utils.CodecUtil;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Optional;

public class FabricatorRecipe implements IFabricatorRecipe {
	public static final MapCodec<FabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Ingredient.CODEC.fieldOf("plan").forGetter(FabricatorRecipe::getPlan),
		SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("molten").forGetter(FabricatorRecipe::getRequiredFluid),
		CodecUtil.CRAFTING_RECIPE_CODEC.fieldOf("recipe").forGetter(FabricatorRecipe::getCraftingGridRecipe)
	).apply(inst, FabricatorRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC,
		FabricatorRecipe::getPlan,
		SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs::optional),
		FabricatorRecipe::getRequiredFluid,
		CodecUtil.CRAFTING_RECIPE_STREAM_CODEC,
		FabricatorRecipe::getCraftingGridRecipe,
		FabricatorRecipe::new
	);

	private final Ingredient plan;
	private final Optional<SizedFluidIngredient> resultFluid;
	private final CraftingRecipe recipe;

	public FabricatorRecipe(Ingredient plan, Optional<SizedFluidIngredient> resultFluid, CraftingRecipe recipe) {
		this.plan = plan;
		this.resultFluid = resultFluid;
		this.recipe = recipe;
	}

	@Override
	public Ingredient getPlan() {
		return this.plan;
	}

	@Override
	public Optional<SizedFluidIngredient> getRequiredFluid() {
		return this.resultFluid;
	}

	@Override
	public CraftingRecipe getCraftingGridRecipe() {
		return this.recipe;
	}

	@Override
	public boolean matches(Level level, FluidStack liquid, ItemStack stack, Container inventory) {
		return (this.resultFluid.isEmpty() || this.resultFluid.get().test(liquid)) && this.plan.test(stack) && this.recipe.matches(FakeCraftingInventory.of(inventory), level);
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return this.recipe.getResultItem(registries);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.FABRICATOR.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.FABRICATOR.type();
	}

	public static class Serializer implements RecipeSerializer<FabricatorRecipe> {
		@Override
		public MapCodec<FabricatorRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
