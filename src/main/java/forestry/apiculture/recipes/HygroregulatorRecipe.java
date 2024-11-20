/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.recipes;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.recipes.IHygroregulatorRecipe;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.RecipeSerializers;

// recipes used by Alveary Hygroregulator
public class HygroregulatorRecipe implements IHygroregulatorRecipe {
	private final ResourceLocation id;
	private final FluidStack liquid;
	private final byte humiditySteps;
	private final byte temperatureSteps;
	private final int retainTime;

	public HygroregulatorRecipe(ResourceLocation id, FluidStack liquid, int retainTime, byte humiditySteps, byte temperatureSteps) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(liquid);
		this.id = id;
		this.liquid = liquid;
		this.retainTime = retainTime;
		this.humiditySteps = humiditySteps;
		this.temperatureSteps = temperatureSteps;
	}

	@Override
	public FluidStack getInputFluid() {
		return liquid;
	}

	@Override
	public int getRetainTime() {
		return this.retainTime;
	}

	@Override
	public byte getHumiditySteps() {
		return humiditySteps;
	}

	@Override
	public byte getTemperatureSteps() {
		return temperatureSteps;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return id;
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
		@Override
		public HygroregulatorRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			FluidStack liquid = RecipeSerializers.deserializeFluid(GsonHelper.getAsJsonObject(json, "liquid"));
			int transferTime = GsonHelper.getAsInt(json, "time");
			byte humiditySteps = GsonHelper.getAsByte(json, "humidity_steps");
			byte temperatureSteps = GsonHelper.getAsByte(json, "temperature_steps");

			return new HygroregulatorRecipe(recipeId, liquid, transferTime, humiditySteps, temperatureSteps);
		}

		@Override
		public HygroregulatorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			FluidStack liquid = FluidStack.readFromPacket(buffer);
			int retainTime = buffer.readVarInt();
			byte humiditySteps = buffer.readByte();
			byte temperatureSteps = buffer.readByte();

			return new HygroregulatorRecipe(recipeId, liquid, retainTime, humiditySteps, temperatureSteps);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, HygroregulatorRecipe recipe) {
			recipe.liquid.writeToPacket(buffer);
			buffer.writeVarInt(recipe.retainTime);
			buffer.writeByte(recipe.humiditySteps);
			buffer.writeByte(recipe.temperatureSteps);
		}
	}
}
