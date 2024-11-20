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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import forestry.api.core.Product;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.core.utils.JsonUtil;
import forestry.factory.features.FactoryRecipeTypes;

public class CentrifugeRecipe implements ICentrifugeRecipe {
	private final ResourceLocation id;
	private final int processingTime;
	private final Ingredient input;
	private final List<Product> products;

	public CentrifugeRecipe(ResourceLocation id, int processingTime, Ingredient input, List<Product> products) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");

		this.id = id;
		this.processingTime = processingTime;
		this.input = input;
		this.products = products;
	}

	@Override
	public Ingredient getInput() {
		return this.input;
	}

	@Override
	public int getProcessingTime() {
		return this.processingTime;
	}

	@Override
	public List<ItemStack> getProducts(RandomSource random) {
		ArrayList<ItemStack> products = new ArrayList<>();

		for (Product entry : this.products) {
			float probability = entry.chance();

			if (probability >= 1.0) {
				products.add(entry.createStack());
			} else if (random.nextFloat() < probability) {
				products.add(entry.createStack());
			}
		}

		return products;
	}

	@Override
	public List<Product> getAllProducts() {
		return this.products;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess access) {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FactoryRecipeTypes.CENTRIFUGE.serializer();
	}

	@Override
	public RecipeType<?> getType() {
		return FactoryRecipeTypes.CENTRIFUGE.type();
	}

	public static class Serializer implements RecipeSerializer<CentrifugeRecipe> {
		@Override
		public CentrifugeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			int processingTime = GsonHelper.getAsInt(json, "time");
			Ingredient input = RecipeSerializers.deserialize(json.get("input"));
			NonNullList<Product> outputs = NonNullList.create();

			for (JsonElement element : GsonHelper.getAsJsonArray(json, "products")) {
				outputs.add(JsonUtil.deserialize(Product.CODEC, element));
			}

			return new CentrifugeRecipe(recipeId, processingTime, input, outputs);
		}

		@Override
		public CentrifugeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			int processingTime = buffer.readVarInt();
			Ingredient input = Ingredient.fromNetwork(buffer);
			List<Product> outputs = RecipeSerializers.read(buffer, Product::fromNetwork);

			return new CentrifugeRecipe(recipeId, processingTime, input, outputs);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, CentrifugeRecipe recipe) {
			buffer.writeVarInt(recipe.processingTime);
			recipe.input.toNetwork(buffer);

			RecipeSerializers.write(buffer, recipe.products, Product::toNetwork);
		}
	}
}
