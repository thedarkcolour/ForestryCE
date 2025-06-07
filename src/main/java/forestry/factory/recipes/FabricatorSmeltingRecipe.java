package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IFabricatorSmeltingRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

public class FabricatorSmeltingRecipe implements IFabricatorSmeltingRecipe {
	public static final MapCodec<FabricatorSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(FabricatorSmeltingRecipe::getInput),
		FluidStack.CODEC.fieldOf("result").forGetter(FabricatorSmeltingRecipe::getResultFluid),
		Codec.INT.fieldOf("melting_point").forGetter(FabricatorSmeltingRecipe::getMeltingPoint)
	).apply(inst, FabricatorSmeltingRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FabricatorSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC,
		FabricatorSmeltingRecipe::getInput,
		FluidStack.STREAM_CODEC,
		FabricatorSmeltingRecipe::getResultFluid,
		ByteBufCodecs.VAR_INT,
		FabricatorSmeltingRecipe::getMeltingPoint,
		FabricatorSmeltingRecipe::new
	);

	private final Ingredient input;
	private final FluidStack product;
	private final int meltingPoint;

	public FabricatorSmeltingRecipe(Ingredient input, FluidStack molten, int meltingPoint) {
		this.input = input;
		this.product = molten;
		this.meltingPoint = meltingPoint;
	}

	@Override
	public Ingredient getInput() {
		return this.input;
	}

	@Override
	public FluidStack getResultFluid() {
		return this.product;
	}

	@Override
	public int getMeltingPoint() {
		return this.meltingPoint;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.FABRICATOR_SMELTING.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.FABRICATOR_SMELTING.type();
	}

	public static class Serializer implements RecipeSerializer<FabricatorSmeltingRecipe> {
		@Override
		public MapCodec<FabricatorSmeltingRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FabricatorSmeltingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
