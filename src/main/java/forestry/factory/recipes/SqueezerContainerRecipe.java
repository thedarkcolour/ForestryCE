package forestry.factory.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.ISqueezerContainerRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class SqueezerContainerRecipe implements ISqueezerContainerRecipe {
	public static final MapCodec<SqueezerContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
	).apply(inst, SqueezerContainerRecipe::new));

	private final ItemStack emptyContainer;
	private final int processingTime;
	private final ItemStack remnants;
	private final float remnantsChance;

	public SqueezerContainerRecipe(ItemStack emptyContainer, int processingTime, ItemStack remnants, float remnantsChance) {
		this.emptyContainer = emptyContainer;
		this.processingTime = processingTime;
		this.remnants = remnants;
		this.remnantsChance = remnantsChance;
	}

	@Override
	public ItemStack getEmptyContainer() {
		return this.emptyContainer;
	}

	@Override
	public List<Ingredient> getInputs() {
		return List.of();
	}

	@Override
	public int getProcessingTime() {
		return this.processingTime;
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
		return FluidStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return this.remnants;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.SQUEEZER_CONTAINER.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.SQUEEZER_CONTAINER.type();
	}

	public static class Serializer implements RecipeSerializer<SqueezerContainerRecipe> {
		@Override
		public MapCodec<SqueezerContainerRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SqueezerContainerRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
