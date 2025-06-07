package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IMoistenerRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class MoistenerRecipe implements IMoistenerRecipe {
	public static final MapCodec<MoistenerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Codec.INT.fieldOf("time").forGetter(MoistenerRecipe::getTimePerItem),
		Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(MoistenerRecipe::getInput),
		ItemStack.CODEC.fieldOf("result").forGetter(MoistenerRecipe::getProduct)
	).apply(inst, MoistenerRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MoistenerRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		MoistenerRecipe::getTimePerItem,
		Ingredient.CONTENTS_STREAM_CODEC,
		MoistenerRecipe::getInput,
		ItemStack.STREAM_CODEC,
		MoistenerRecipe::getProduct,
		MoistenerRecipe::new
	);

	private final int timePerItem;
	private final Ingredient resource;
	private final ItemStack product;

	public MoistenerRecipe(int timePerItem, Ingredient resource, ItemStack product) {
		this.timePerItem = timePerItem;
		this.resource = resource;
		this.product = product;
	}

	@Override
	public int getTimePerItem() {
		return this.timePerItem;
	}

	@Override
	public Ingredient getInput() {
		return this.resource;
	}

	@Override
	public ItemStack getProduct() {
		return this.product;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return this.product;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.MOISTENER.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.MOISTENER.type();
	}

	public static class Serializer implements RecipeSerializer<MoistenerRecipe> {
		@Override
		public MapCodec<MoistenerRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, MoistenerRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
