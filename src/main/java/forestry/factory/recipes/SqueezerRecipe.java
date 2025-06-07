package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.ISqueezerRecipe;
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

import java.util.List;

public class SqueezerRecipe implements ISqueezerRecipe {
	public static final MapCodec<SqueezerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Codec.INT.fieldOf("time").forGetter(SqueezerRecipe::getProcessingTime),
		// todo use SizedIngredient
		Ingredient.CODEC.listOf().fieldOf("resources").forGetter(SqueezerRecipe::getInputs),
		FluidStack.OPTIONAL_CODEC.fieldOf("output").forGetter(SqueezerRecipe::getFluidOutput),
		ItemStack.OPTIONAL_CODEC.fieldOf("remnant").forGetter(SqueezerRecipe::getRemnants),
		Codec.FLOAT.fieldOf("chance").forGetter(SqueezerRecipe::getRemnantsChance)
	).apply(inst, SqueezerRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, SqueezerRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		SqueezerRecipe::getProcessingTime,
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
		SqueezerRecipe::getInputs,
		FluidStack.OPTIONAL_STREAM_CODEC,
		SqueezerRecipe::getFluidOutput,
		ItemStack.OPTIONAL_STREAM_CODEC,
		SqueezerRecipe::getRemnants,
		ByteBufCodecs.FLOAT,
		SqueezerRecipe::getRemnantsChance,
		SqueezerRecipe::new
	);

	private final int processingTime;
	private final List<Ingredient> resources;
	private final FluidStack fluidOutput;
	private final ItemStack remnants;
	private final float remnantsChance;

	public SqueezerRecipe(int processingTime, List<Ingredient> resources, FluidStack fluidOutput, ItemStack remnants, float remnantsChance) {
		this.processingTime = processingTime;
		this.resources = resources;
		this.fluidOutput = fluidOutput;
		this.remnants = remnants;
		this.remnantsChance = remnantsChance;
	}

	@Override
	public List<Ingredient> getInputs() {
		return this.resources;
	}

	@Override
	public ItemStack getRemnants() {
		return this.remnants;
	}

	@Override
	public float getRemnantsChance() {
		return this.remnantsChance;
	}

	@Override
	public FluidStack getFluidOutput() {
		return this.fluidOutput;
	}

	@Override
	public int getProcessingTime() {
		return this.processingTime;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.SQUEEZER.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.SQUEEZER.type();
	}

	public static class Serializer implements RecipeSerializer<SqueezerRecipe> {
		@Override
		public MapCodec<SqueezerRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SqueezerRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
