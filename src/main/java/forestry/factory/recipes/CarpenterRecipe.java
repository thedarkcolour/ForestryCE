package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.ICarpenterRecipe;
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

public class CarpenterRecipe implements ICarpenterRecipe {
	public static final MapCodec<CarpenterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Codec.INT.fieldOf("time").forGetter(CarpenterRecipe::getPackagingTime),
		SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("liquid").forGetter(CarpenterRecipe::getInputFluid),
		Ingredient.CODEC.optionalFieldOf("box", Ingredient.EMPTY).forGetter(CarpenterRecipe::getBox),
		CodecUtil.CRAFTING_RECIPE_CODEC.fieldOf("recipe").forGetter(CarpenterRecipe::getCraftingGridRecipe),
		ItemStack.OPTIONAL_CODEC.fieldOf("result").forGetter(r -> r.result)
	).apply(inst, CarpenterRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		CarpenterRecipe::getPackagingTime,
		SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs::optional),
		CarpenterRecipe::getInputFluid,
		Ingredient.CONTENTS_STREAM_CODEC,
		CarpenterRecipe::getBox,
		CodecUtil.CRAFTING_RECIPE_STREAM_CODEC,
		CarpenterRecipe::getCraftingGridRecipe,
		ItemStack.STREAM_CODEC,
		r -> r.result,
		CarpenterRecipe::new
	);

	private final int packagingTime;
	private final Optional<SizedFluidIngredient> liquid;
	private final Ingredient box;
	private final CraftingRecipe recipe;
	private final ItemStack result;

	public CarpenterRecipe(int packagingTime, Optional<SizedFluidIngredient> liquid, Ingredient box, CraftingRecipe recipe, ItemStack result) {
		this.packagingTime = packagingTime;
		this.liquid = liquid;
		this.box = box;
		this.recipe = recipe;
		this.result = result;
	}

	@Override
	public int getPackagingTime() {
		return this.packagingTime;
	}

	@Override
	public Ingredient getBox() {
		return this.box;
	}

	@Override
	public Optional<SizedFluidIngredient> getInputFluid() {
		return this.liquid;
	}

	@Override
	public CraftingRecipe getCraftingGridRecipe() {
		return this.recipe;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return !this.result.isEmpty() ? this.result : this.recipe.getResultItem(registries);
	}

	@Override
	public boolean matches(FluidStack fluid, ItemStack boxStack, Container craftingInventory, Level level) {
		if (this.liquid.isPresent()) {
			if (fluid.isEmpty() || !this.liquid.get().test(fluid)) {
				return false;
			}
		}

		Ingredient box = this.box;
		if (!box.isEmpty() && !box.test(boxStack)) {
			return false;
		}

		return this.recipe.matches(FakeCraftingInventory.of(craftingInventory), level);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.CARPENTER.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.CARPENTER.type();
	}

	public static class Serializer implements RecipeSerializer<CarpenterRecipe> {
		@Override
		public MapCodec<CarpenterRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CarpenterRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
