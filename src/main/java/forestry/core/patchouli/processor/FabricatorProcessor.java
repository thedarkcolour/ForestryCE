package forestry.core.patchouli.processor;

import java.util.Arrays;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import forestry.api.recipes.IFabricatorRecipe;
import forestry.core.utils.ModUtil;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryRecipeTypes;

import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class FabricatorProcessor implements IComponentProcessor {
	@Nullable
	protected IFabricatorRecipe recipe;

	@Override
	public void setup(Level level, IVariableProvider variables) {
		ItemStack stack = variables.get("item").as(ItemStack.class, ItemStack.EMPTY);

		this.recipe = RecipeUtils.getRecipeByOutput(FactoryRecipeTypes.FABRICATOR, level.registryAccess(), stack);
	}

	@Override
	public IVariable process(Level level, String key) {
		Preconditions.checkNotNull(recipe);
		if (key.equals("output")) {
			return IVariable.from(this.recipe.getCraftingGridRecipe().getResultItem(level.registryAccess()));
		} else if (key.equals("fluid")) {
			return IVariable.wrap(ModUtil.getRegistryName(this.recipe.getResultFluid().getFluid()).toString());
		} else if (key.equals("fluidAmount")) {
			return IVariable.wrap(this.recipe.getResultFluid().getAmount());
		} else if (key.startsWith("ingredient")) {
			int index = Integer.parseInt(key.substring("ingredient".length()));
            if (index < 1 || index > 9) {
                return IVariable.empty();
            }

			Ingredient ingredient;
			try {
				ingredient = this.recipe.getCraftingGridRecipe().getIngredients().get(index - 1);
			} catch (Exception e) {
				ingredient = Ingredient.EMPTY;
			}
			return IVariable.from(ingredient.getItems());
		} else if (key.equals("plan")) {
			return IVariable.from(this.recipe.getPlan());
		} else if (key.equals("metal")) {
			if (ModUtil.getRegistryName(this.recipe.getResultFluid().getFluid()).getPath().contains("glass")) {
				return IVariable.from(new ItemStack(Items.SAND));
			}

			return RecipeUtils.getRecipes(RecipeUtils.getRecipeManager(), FactoryRecipeTypes.FABRICATOR_SMELTING)
					.filter(recipe -> recipe.getResultFluid().isFluidEqual(this.recipe.getResultFluid()))
					.flatMap(r -> Arrays.stream(r.getInput().getItems()))
					.findFirst()
					.map(IVariable::from)
					.orElseGet(IVariable::empty);
		} else {
			return IVariable.empty();
		}
	}
}
