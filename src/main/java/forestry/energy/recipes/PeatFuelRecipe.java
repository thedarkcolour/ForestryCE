package forestry.energy.recipes;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IPeatFuel;
import forestry.energy.features.EnergyRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class PeatFuelRecipe implements IPeatFuel {
	private static final MapCodec<PeatFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(PeatFuelRecipe::getId),
		Ingredient.CODEC_NONEMPTY.fieldOf("fuel").forGetter(PeatFuelRecipe::getInput),
		Codec.INT.fieldOf("power_per_cycle").forGetter(PeatFuelRecipe::getPowerPerCycle),
		Codec.INT.fieldOf("burn_duration").forGetter(PeatFuelRecipe::getBurnDuration)
	).apply(instance, PeatFuelRecipe::new));
	private static final StreamCodec<RegistryFriendlyByteBuf, PeatFuelRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final ResourceLocation id;
	private final Ingredient fuel;
	private final int powerPerCycle;
	private final int burnDuration;

	public PeatFuelRecipe(ResourceLocation id, Ingredient fuel, int powerPerCycle, int burnDuration) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(fuel, "Peat fuel ingredient cannot be null");
		Preconditions.checkArgument(!fuel.isEmpty(), "Peat fuel ingredient cannot be empty");
		this.id = id;
		this.fuel = fuel;
		this.powerPerCycle = powerPerCycle;
		this.burnDuration = burnDuration;
	}

	@Override
	public Ingredient getInput() {
		return this.fuel;
	}

	@Override
	public int getPowerPerCycle() {
		return this.powerPerCycle;
	}

	@Override
	public int getBurnDuration() {
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
		return EnergyRecipeTypes.PEAT_FUEL.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return EnergyRecipeTypes.PEAT_FUEL.type();
	}

	public static class Serializer implements RecipeSerializer<PeatFuelRecipe> {
		@Override
		public MapCodec<PeatFuelRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, PeatFuelRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static PeatFuelRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			ResourceLocation recipeId = ResourceLocation.STREAM_CODEC.decode(buffer);
			Ingredient fuel = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			int powerPerCycle = ByteBufCodecs.VAR_INT.decode(buffer);
			int burnDuration = ByteBufCodecs.VAR_INT.decode(buffer);
			return new PeatFuelRecipe(recipeId, fuel, powerPerCycle, burnDuration);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, PeatFuelRecipe recipe) {
			ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.fuel);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.powerPerCycle);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.burnDuration);
		}
	}
}
