package forestry.factory.recipes;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IRainSubstrate;
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

public class RainSubstrateRecipe implements IRainSubstrate {
	private static final MapCodec<RainSubstrateRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(RainSubstrateRecipe::getId),
		Ingredient.CODEC_NONEMPTY.fieldOf("substrate").forGetter(RainSubstrateRecipe::getInput),
		Codec.INT.optionalFieldOf("duration", 0).forGetter(RainSubstrateRecipe::duration),
		Codec.FLOAT.fieldOf("speed").forGetter(RainSubstrateRecipe::speed),
		Codec.BOOL.optionalFieldOf("reverse", false).forGetter(RainSubstrateRecipe::reverse)
	).apply(instance, RainSubstrateRecipe::new));
	private static final StreamCodec<RegistryFriendlyByteBuf, RainSubstrateRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final ResourceLocation id;
	private final Ingredient substrate;
	private final int duration;
	private final float speed;
	private final boolean reverse;

	public RainSubstrateRecipe(ResourceLocation id, Ingredient substrate, int duration, float speed, boolean reverse) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(substrate, "Rain substrate ingredient cannot be null");
		Preconditions.checkArgument(!substrate.isEmpty(), "Rain substrate ingredient cannot be empty");
		this.id = id;
		this.substrate = substrate;
		this.duration = duration;
		this.speed = speed;
		this.reverse = reverse;
	}

	@Override
	public Ingredient getInput() {
		return this.substrate;
	}

	@Override
	public int duration() {
		return this.duration;
	}

	@Override
	public float speed() {
		return this.speed;
	}

	@Override
	public boolean reverse() {
		return this.reverse;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider lookupProvider) {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.RAIN_SUBSTRATE.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.RAIN_SUBSTRATE.type();
	}

	public static class Serializer implements RecipeSerializer<RainSubstrateRecipe> {
		@Override
		public MapCodec<RainSubstrateRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, RainSubstrateRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static RainSubstrateRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			ResourceLocation recipeId = ResourceLocation.STREAM_CODEC.decode(buffer);
			Ingredient substrate = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			int duration = ByteBufCodecs.VAR_INT.decode(buffer);
			float speed = ByteBufCodecs.FLOAT.decode(buffer);
			boolean reverse = ByteBufCodecs.BOOL.decode(buffer);
			return new RainSubstrateRecipe(recipeId, substrate, duration, speed, reverse);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, RainSubstrateRecipe recipe) {
			ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.substrate);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.duration);
			ByteBufCodecs.FLOAT.encode(buffer, recipe.speed);
			ByteBufCodecs.BOOL.encode(buffer, recipe.reverse);
		}
	}
}
