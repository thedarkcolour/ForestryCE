package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IStillRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class StillRecipe implements IStillRecipe {
	public static final MapCodec<StillRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Codec.INT.fieldOf("cycles").forGetter(StillRecipe::getCyclesPerUnit),
		SizedFluidIngredient.FLAT_CODEC.fieldOf("input").forGetter(StillRecipe::getInput),
		FluidStack.CODEC.fieldOf("output").forGetter(StillRecipe::getOutput)
	).apply(inst, StillRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, StillRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		StillRecipe::getCyclesPerUnit,
		SizedFluidIngredient.STREAM_CODEC,
		StillRecipe::getInput,
		FluidStack.STREAM_CODEC,
		StillRecipe::getOutput,
		StillRecipe::new
	);

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
	public SizedFluidIngredient getInput() {
		return this.input;
	}

	@Override
	public FluidStack getOutput() {
		return this.output;
	}

	@Override
	public boolean matches(FluidStack input) {
		return this.input.test(input);
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
		public MapCodec<StillRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, StillRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
