package forestry.api.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

public interface IForestryRecipe extends Recipe<RecipeInput> {
	@Deprecated
	@Override
	default boolean matches(RecipeInput inv, Level level) {
		return false;
	}

	@Deprecated
	@Override
	default ItemStack assemble(RecipeInput inv, HolderLookup.Provider lookup) {
		return ItemStack.EMPTY;
	}

	@Deprecated
	@Override
	default boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Deprecated
	@Override
	default NonNullList<ItemStack> getRemainingItems(RecipeInput inv) {
		return NonNullList.create();
	}

	@Deprecated
	@Override
	default NonNullList<Ingredient> getIngredients() {
		return NonNullList.create();
	}

	@Deprecated
	@Override
	default boolean isSpecial() {
		return true;
	}

	@Deprecated
	@Override
	default String getGroup() {
		return "forestry";
	}

	@Deprecated
	@Override
	default ItemStack getToastSymbol() {
		return ItemStack.EMPTY;
	}
}
