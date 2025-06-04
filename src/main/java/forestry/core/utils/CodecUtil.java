package forestry.core.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import java.util.function.Function;

public class CodecUtil {
	public static final Codec<CraftingRecipe> CRAFTING_RECIPE_CODEC = Recipe.CODEC.flatXmap(r -> r instanceof CraftingRecipe ? DataResult.success((CraftingRecipe) r) : DataResult.error(() -> "Recipe must be a CraftingRecipe"), DataResult::success);
	public static final StreamCodec<RegistryFriendlyByteBuf, CraftingRecipe> CRAFTING_RECIPE_STREAM_CODEC = Recipe.STREAM_CODEC.map(CraftingRecipe.class::cast, Function.identity());
}
