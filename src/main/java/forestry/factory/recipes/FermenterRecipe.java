package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IFermenterRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class FermenterRecipe implements IFermenterRecipe {
	public static final MapCodec<FermenterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Ingredient.CODEC.fieldOf("input").forGetter(FermenterRecipe::getInputItem),
		FluidIngredient.CODEC.fieldOf("input_fluid").forGetter(FermenterRecipe::getInputFluid),
		Codec.INT.fieldOf("fermentation_value").forGetter(FermenterRecipe::getFermentationValue),
		Codec.FLOAT.fieldOf("modifier").forGetter(FermenterRecipe::getModifier),
		BuiltInRegistries.FLUID.byNameCodec().fieldOf("output_fluid").forGetter(FermenterRecipe::getOutputFluid)
	).apply(inst, FermenterRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FermenterRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC,
		FermenterRecipe::getInputItem,
		FluidIngredient.STREAM_CODEC,
		FermenterRecipe::getInputFluid,
		ByteBufCodecs.VAR_INT,
		FermenterRecipe::getFermentationValue,
		ByteBufCodecs.FLOAT,
		FermenterRecipe::getModifier,
		ByteBufCodecs.registry(Registries.FLUID),
		FermenterRecipe::getOutputFluid,
		FermenterRecipe::new
	);

	private final Ingredient input;
	private final FluidIngredient inputFluid;
	private final int fermentationValue;
	private final float modifier;
	private final Fluid outputFluid;

	public FermenterRecipe(Ingredient input, FluidIngredient inputFluid, int fermentationValue, float modifier, Fluid outputFluid) {
		this.input = input;
		this.inputFluid = inputFluid;
		this.fermentationValue = fermentationValue;
		this.modifier = modifier;
		this.outputFluid = outputFluid;
	}

	@Override
	public Ingredient getInputItem() {
		return this.input;
	}

	@Override
	public FluidIngredient getInputFluid() {
		return this.inputFluid;
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
	public Fluid getOutputFluid() {
		return this.outputFluid;
	}

	@Override
	public boolean matches(ItemStack inputItem, FluidStack inputFluid) {
		return this.input.test(inputItem) && this.inputFluid.test(inputFluid);
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
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
		public MapCodec<FermenterRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FermenterRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
