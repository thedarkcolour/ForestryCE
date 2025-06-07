package forestry.factory.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.core.Product;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeRecipe implements ICentrifugeRecipe {
	public static final MapCodec<CentrifugeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Codec.INT.fieldOf("time").forGetter(CentrifugeRecipe::getProcessingTime),
		Ingredient.CODEC.fieldOf("input").forGetter(CentrifugeRecipe::getInput),
		Product.CODEC.listOf().fieldOf("products").forGetter(CentrifugeRecipe::getAllProducts)
	).apply(inst, CentrifugeRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugeRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		CentrifugeRecipe::getProcessingTime,
		Ingredient.CONTENTS_STREAM_CODEC,
		CentrifugeRecipe::getInput,
		Product.STREAM_CODEC.apply(ByteBufCodecs.list()),
		CentrifugeRecipe::getAllProducts,
		CentrifugeRecipe::new
	);

	private final int processingTime;
	private final Ingredient input;
	private final List<Product> products;

	public CentrifugeRecipe(int processingTime, Ingredient input, List<Product> products) {
		this.processingTime = processingTime;
		this.input = input;
		this.products = products;
	}

	@Override
	public Ingredient getInput() {
		return this.input;
	}

	@Override
	public int getProcessingTime() {
		return this.processingTime;
	}

	@Override
	public List<ItemStack> getProducts(RandomSource random) {
		ArrayList<ItemStack> products = new ArrayList<>();

		for (Product entry : this.products) {
			float probability = entry.chance();

			if (probability >= 1.0) {
				products.add(entry.createStack());
			} else if (random.nextFloat() < probability) {
				products.add(entry.createStack());
			}
		}

		return products;
	}

	@Override
	public List<Product> getAllProducts() {
		return this.products;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider access) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.CENTRIFUGE.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.CENTRIFUGE.type();
	}

	public static class Serializer implements RecipeSerializer<CentrifugeRecipe> {
		@Override
		public MapCodec<CentrifugeRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CentrifugeRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
