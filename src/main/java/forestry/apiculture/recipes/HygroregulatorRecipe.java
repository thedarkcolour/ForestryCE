package forestry.apiculture.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.recipes.IHygroregulatorRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

// recipes used by Alveary Hygroregulator
public class HygroregulatorRecipe implements IHygroregulatorRecipe {
	public static final MapCodec<HygroregulatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		SizedFluidIngredient.FLAT_CODEC.fieldOf("input_fluid").forGetter(HygroregulatorRecipe::getInputFluid),
		Codec.INT.fieldOf("retain_time").forGetter(HygroregulatorRecipe::getRetainTime),
		Codec.BYTE.fieldOf("humidity_steps").forGetter(HygroregulatorRecipe::getHumiditySteps),
		Codec.BYTE.fieldOf("temperature_steps").forGetter(HygroregulatorRecipe::getTemperatureSteps)
	).apply(inst, HygroregulatorRecipe::new));
	private static final StreamCodec<RegistryFriendlyByteBuf, HygroregulatorRecipe> STREAM_CODEC = StreamCodec.of(Serializer::encode, Serializer::decode);

	private final SizedFluidIngredient liquid;
	private final byte humiditySteps;
	private final byte temperatureSteps;
	private final int retainTime;

	public HygroregulatorRecipe(SizedFluidIngredient liquid, int retainTime, byte humiditySteps, byte temperatureSteps) {
		this.liquid = liquid;
		this.retainTime = retainTime;
		this.humiditySteps = humiditySteps;
		this.temperatureSteps = temperatureSteps;
	}

	@Override
	public SizedFluidIngredient getInputFluid() {
		return this.liquid;
	}

	@Override
	public int getRetainTime() {
		return this.retainTime;
	}

	@Override
	public byte getHumiditySteps() {
		return this.humiditySteps;
	}

	@Override
	public byte getTemperatureSteps() {
		return this.temperatureSteps;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.HYGROREGULATOR.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.HYGROREGULATOR.type();
	}

	public static class Serializer implements RecipeSerializer<HygroregulatorRecipe> {
		public static void encode(RegistryFriendlyByteBuf buffer, HygroregulatorRecipe recipe) {
			SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.liquid);
			buffer.writeVarInt(recipe.retainTime);
			buffer.writeByte(recipe.humiditySteps);
			buffer.writeByte(recipe.temperatureSteps);
		}

		public static HygroregulatorRecipe decode(RegistryFriendlyByteBuf buffer) {
			SizedFluidIngredient liquid = SizedFluidIngredient.STREAM_CODEC.decode(buffer);
			int retainTime = buffer.readVarInt();
			byte humiditySteps = buffer.readByte();
			byte temperatureSteps = buffer.readByte();

			return new HygroregulatorRecipe(liquid, retainTime, humiditySteps, temperatureSteps);
		}

		@Override
		public MapCodec<HygroregulatorRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, HygroregulatorRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
