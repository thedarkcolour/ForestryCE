package forestry.factory.recipes;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IFermenterFuel;
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

public class FermenterFuelRecipe implements IFermenterFuel {
	private static final MapCodec<FermenterFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(FermenterFuelRecipe::getId),
		Ingredient.CODEC_NONEMPTY.fieldOf("fuel").forGetter(FermenterFuelRecipe::getInput),
		Codec.INT.fieldOf("ferment_per_cycle").forGetter(FermenterFuelRecipe::fermentPerCycle),
		Codec.INT.fieldOf("burn_duration").forGetter(FermenterFuelRecipe::burnDuration)
	).apply(instance, FermenterFuelRecipe::new));
	private static final StreamCodec<RegistryFriendlyByteBuf, FermenterFuelRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final ResourceLocation id;
	private final Ingredient fuel;
	private final int fermentPerCycle;
	private final int burnDuration;

	public FermenterFuelRecipe(ResourceLocation id, Ingredient fuel, int fermentPerCycle, int burnDuration) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(fuel, "Fermenter fuel ingredient cannot be null");
		Preconditions.checkArgument(!fuel.isEmpty(), "Fermenter fuel ingredient cannot be empty");
		this.id = id;
		this.fuel = fuel;
		this.fermentPerCycle = fermentPerCycle;
		this.burnDuration = burnDuration;
	}

	@Override
	public Ingredient getInput() {
		return this.fuel;
	}

	@Override
	public int fermentPerCycle() {
		return this.fermentPerCycle;
	}

	@Override
	public int burnDuration() {
		return this.burnDuration;
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
		return FactoryRecipeTypes.FERMENTER_FUEL.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.FERMENTER_FUEL.type();
	}

	public static class Serializer implements RecipeSerializer<FermenterFuelRecipe> {
		@Override
		public MapCodec<FermenterFuelRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FermenterFuelRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static FermenterFuelRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			ResourceLocation recipeId = ResourceLocation.STREAM_CODEC.decode(buffer);
			Ingredient fuel = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			int fermentPerCycle = ByteBufCodecs.VAR_INT.decode(buffer);
			int burnDuration = ByteBufCodecs.VAR_INT.decode(buffer);
			return new FermenterFuelRecipe(recipeId, fuel, fermentPerCycle, burnDuration);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, FermenterFuelRecipe recipe) {
			ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.fuel);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.fermentPerCycle);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.burnDuration);
		}
	}
}
