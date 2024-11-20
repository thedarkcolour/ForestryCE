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
package forestry.factory.recipes;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.recipes.ISqueezerContainerRecipe;
import forestry.factory.features.FactoryRecipeTypes;

public class SqueezerContainerRecipe implements ISqueezerContainerRecipe {
	private final ResourceLocation id;
	private final ItemStack emptyContainer;
	private final int processingTime;
	private final ItemStack remnants;
	private final float remnantsChance;

	public SqueezerContainerRecipe(ResourceLocation id, ItemStack emptyContainer, int processingTime, ItemStack remnants, float remnantsChance) {
		this.id = id;
		Preconditions.checkNotNull(emptyContainer);
		Preconditions.checkArgument(!emptyContainer.isEmpty());
		Preconditions.checkNotNull(remnants);

		this.emptyContainer = emptyContainer;
		this.processingTime = processingTime;
		this.remnants = remnants;
		this.remnantsChance = remnantsChance;
	}

	@Override
	public ItemStack getEmptyContainer() {
		return emptyContainer;
	}

	@Override
	public List<Ingredient> getInputs() {
		return List.of();
	}

	@Override
	public int getProcessingTime() {
		return processingTime;
	}

	@Override
	public ItemStack getRemnants() {
		return remnants;
	}

	@Override
	public float getRemnantsChance() {
		return remnantsChance;
	}

	@Override
	public FluidStack getFluidOutput() {
		return FluidStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return this.remnants;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.SQUEEZER_CONTAINER.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.SQUEEZER_CONTAINER.type();
	}

	public static class Serializer implements RecipeSerializer<SqueezerContainerRecipe> {

		@Override
		public SqueezerContainerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			ItemStack emptyContainer = RecipeSerializers.item(GsonHelper.getAsJsonObject(json, "container"));
			int processingTime = GsonHelper.getAsInt(json, "time");
			ItemStack remnants = RecipeSerializers.item(GsonHelper.getAsJsonObject(json, "remnants"));
			float remnantsChance = GsonHelper.getAsFloat(json, "remnantsChance");

			return new SqueezerContainerRecipe(recipeId, emptyContainer, processingTime, remnants, remnantsChance);
		}

		@Override
		public SqueezerContainerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			ItemStack emptyContainer = buffer.readItem();
			int processingTime = buffer.readVarInt();
			ItemStack remnants = buffer.readItem();
			float remnantsChance = buffer.readFloat();

			return new SqueezerContainerRecipe(recipeId, emptyContainer, processingTime, remnants, remnantsChance);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, SqueezerContainerRecipe recipe) {
			buffer.writeItem(recipe.emptyContainer);
			buffer.writeVarInt(recipe.processingTime);
			buffer.writeItem(recipe.remnants);
			buffer.writeFloat(recipe.remnantsChance);
		}
	}
}
