package forestry.energy.recipes;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IBiogasFuel;
import forestry.energy.features.EnergyRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class BiogasFuelRecipe implements IBiogasFuel {
	private static final MapCodec<BiogasFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(BiogasFuelRecipe::getId),
		FluidIngredient.CODEC.fieldOf("fluid").forGetter(BiogasFuelRecipe::getFluidInput),
		Codec.INT.fieldOf("power_per_cycle").forGetter(BiogasFuelRecipe::getPowerPerCycle),
		Codec.INT.fieldOf("burn_duration").forGetter(BiogasFuelRecipe::getBurnDuration),
		Codec.INT.optionalFieldOf("dissipation_multiplier", 1).forGetter(BiogasFuelRecipe::getDissipationMultiplier)
	).apply(instance, BiogasFuelRecipe::new));
	private static final StreamCodec<RegistryFriendlyByteBuf, BiogasFuelRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final ResourceLocation id;
	private final FluidIngredient fluid;
	private final int powerPerCycle;
	private final int burnDuration;
	private final int dissipationMultiplier;

	public BiogasFuelRecipe(ResourceLocation id, FluidIngredient fluid, int powerPerCycle, int burnDuration, int dissipationMultiplier) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(fluid, "Biogas fuel fluid cannot be null");
		this.id = id;
		this.fluid = fluid;
		this.powerPerCycle = powerPerCycle;
		this.burnDuration = burnDuration;
		this.dissipationMultiplier = dissipationMultiplier;
	}

	@Override
	public FluidIngredient getFluidInput() {
		return this.fluid;
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
	public int getDissipationMultiplier() {
		return this.dissipationMultiplier;
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
		return EnergyRecipeTypes.BIOGAS_FUEL.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return EnergyRecipeTypes.BIOGAS_FUEL.type();
	}

	public static class Serializer implements RecipeSerializer<BiogasFuelRecipe> {
		@Override
		public MapCodec<BiogasFuelRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BiogasFuelRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static BiogasFuelRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			ResourceLocation recipeId = ResourceLocation.STREAM_CODEC.decode(buffer);
			FluidIngredient fluid = FluidIngredient.STREAM_CODEC.decode(buffer);
			int powerPerCycle = ByteBufCodecs.VAR_INT.decode(buffer);
			int burnDuration = ByteBufCodecs.VAR_INT.decode(buffer);
			int dissipationMultiplier = ByteBufCodecs.VAR_INT.decode(buffer);
			return new BiogasFuelRecipe(recipeId, fluid, powerPerCycle, burnDuration, dissipationMultiplier);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, BiogasFuelRecipe recipe) {
			ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);
			FluidIngredient.STREAM_CODEC.encode(buffer, recipe.fluid);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.powerPerCycle);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.burnDuration);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.dissipationMultiplier);
		}
	}
}
