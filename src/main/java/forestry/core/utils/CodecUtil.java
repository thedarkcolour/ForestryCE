package forestry.core.utils;

import com.mojang.serialization.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.stream.Stream;

public class CodecUtil {
	public static final Codec<CraftingRecipe> CRAFTING_RECIPE_CODEC = new CraftingRecipeCodec().codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, CraftingRecipe> CRAFTING_RECIPE_STREAM_CODEC = StreamCodec.of(CodecUtil::encodeCraftingRecipe, CodecUtil::decodeCraftingRecipe);

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void encodeCraftingRecipe(RegistryFriendlyByteBuf buffer, CraftingRecipe recipe) {
		RecipeSerializer serializer = recipe.getSerializer();
		buffer.writeBoolean(serializer == RecipeSerializer.SHAPELESS_RECIPE);
		serializer.streamCodec().encode(buffer, recipe);
	}

	private static CraftingRecipe decodeCraftingRecipe(RegistryFriendlyByteBuf buffer) {
		return (buffer.readBoolean() ? RecipeSerializer.SHAPELESS_RECIPE : RecipeSerializer.SHAPED_RECIPE).streamCodec().decode(buffer);
	}

	// why did i write this
	private static class CraftingRecipeCodec extends MapCodec<CraftingRecipe> {
		// using static references is safe maybe probably
		private static final MapCodec<? extends CraftingRecipe> SHAPED = RecipeSerializer.SHAPED_RECIPE.codec();
		private static final MapCodec<? extends CraftingRecipe> SHAPELESS = RecipeSerializer.SHAPELESS_RECIPE.codec();

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("shapeless"));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> DataResult<CraftingRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			T isShapelessPart = input.get("shapeless");

			if (isShapelessPart == null) {
				// default to shaped recipe
				return (DataResult<CraftingRecipe>) SHAPED.decode(ops, input);
			}

			return (DataResult<CraftingRecipe>) Codec.BOOL
				.decode(ops, isShapelessPart)
				.flatMap(isShapeless -> (isShapeless.getFirst() ? SHAPELESS : SHAPED).decode(ops, input));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> RecordBuilder<T> encode(CraftingRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			if (input.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE) {
				return ((MapCodec<CraftingRecipe>) SHAPELESS).encode(input, ops, prefix)
					.add("shapeless", Codec.BOOL.encodeStart(ops, true));
			} else if (input.getSerializer() == RecipeSerializer.SHAPED_RECIPE) {
				// no need to serialize the default value of "shapeless": "false"
				return ((MapCodec<CraftingRecipe>) SHAPED).encode(input, ops, prefix);
			}
			// not gonna bother with other types of recipes for now. if someone wants it, open an issue.
			return prefix.withErrorsFrom(DataResult.error(() -> "Error: Recipe was not of serializer minecraft:crafting_shaped or minecraft:crafting_shapeless:" + input));
		}
	}
}
