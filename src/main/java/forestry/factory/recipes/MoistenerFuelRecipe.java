package forestry.factory.recipes;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IMoistenerFuel;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class MoistenerFuelRecipe implements IMoistenerFuel {
	private static final MapCodec<MoistenerFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(MoistenerFuelRecipe::getId),
		Ingredient.CODEC_NONEMPTY.fieldOf("resource").forGetter(MoistenerFuelRecipe::getInput),
		ItemStack.STRICT_CODEC.fieldOf("product").forGetter(MoistenerFuelRecipe::product),
		Codec.INT.fieldOf("stage").forGetter(MoistenerFuelRecipe::stage),
		Codec.INT.fieldOf("moistener_value").forGetter(MoistenerFuelRecipe::moistenerValue)
	).apply(instance, MoistenerFuelRecipe::new));
	private static final StreamCodec<RegistryFriendlyByteBuf, MoistenerFuelRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final ResourceLocation id;
	private final Ingredient resource;
	private final ItemStack product;
	private final int stage;
	private final int moistenerValue;

	public MoistenerFuelRecipe(ResourceLocation id, Ingredient resource, ItemStack product, int stage, int moistenerValue) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(resource, "Moistener resource ingredient cannot be null");
		Preconditions.checkArgument(!resource.isEmpty(), "Moistener resource ingredient cannot be empty");
		Preconditions.checkArgument(!product.isEmpty(), "Moistener product cannot be empty");
		this.id = id;
		this.resource = resource;
		this.product = product;
		this.stage = stage;
		this.moistenerValue = moistenerValue;
	}

	@Override
	public Ingredient getInput() {
		return this.resource;
	}

	@Override
	public ItemStack product() {
		return this.product;
	}

	@Override
	public int stage() {
		return this.stage;
	}

	@Override
	public int moistenerValue() {
		return this.moistenerValue;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider lookupProvider) {
		return this.product;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.MOISTENER_FUEL.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.MOISTENER_FUEL.type();
	}

	public static class Serializer implements RecipeSerializer<MoistenerFuelRecipe> {
		@Override
		public MapCodec<MoistenerFuelRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, MoistenerFuelRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static MoistenerFuelRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			ResourceLocation recipeId = ResourceLocation.STREAM_CODEC.decode(buffer);
			Ingredient resource = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			ItemStack product = ItemStack.STREAM_CODEC.decode(buffer);
			int stage = ByteBufCodecs.VAR_INT.decode(buffer);
			int moistenerValue = ByteBufCodecs.VAR_INT.decode(buffer);
			return new MoistenerFuelRecipe(recipeId, resource, product, stage, moistenerValue);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, MoistenerFuelRecipe recipe) {
			ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.resource);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.product);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.stage);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.moistenerValue);
		}
	}
}
