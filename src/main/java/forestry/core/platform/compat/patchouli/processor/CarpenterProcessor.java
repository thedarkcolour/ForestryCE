package forestry.core.platform.compat.patchouli.processor;

import forestry.Forestry;
import forestry.api.core.machines.ICarpenterRecipe;
import forestry.core.platform.util.ModUtil;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class CarpenterProcessor implements IComponentProcessor {
	protected ItemStack output = ItemStack.EMPTY;
	@Nullable
	protected ICarpenterRecipe recipe;

	@Override
	public void setup(Level level, IVariableProvider variables) {
		try {
			this.output = variables.get("item", level.registryAccess()).as(ItemStack.class, ItemStack.EMPTY);
			this.recipe = RecipeUtils.getRecipeByOutput(FactoryRecipeTypes.CARPENTER, level.registryAccess(), this.output);
		} catch (RuntimeException e) {
			this.recipe = null;
			Forestry.LOGGER.error("Failed to look up the carpenter recipe for {} in the Forester's Manual", this.output, e);
			return;
		}

		if (this.recipe == null) {
			Forestry.LOGGER.warn("No carpenter recipe with output {}, showing an empty recipe in the guide", this.output);
		}
	}

	@Override
	public IVariable process(Level level, String key) {
		try {
			return processRecipe(level, key);
		} catch (RuntimeException e) {
			Forestry.LOGGER.error("Failed to read {} of the carpenter recipe for {} in the Forester's Manual", key, this.output, e);
			return IVariable.empty();
		}
	}

	private IVariable processRecipe(Level level, String key) {
		HolderLookup.Provider registries = level.registryAccess();
		// a datapack can remove or replace the recipe this page was written for
		if (this.recipe == null) {
			return key.equals("output") ? IVariable.from(this.output, registries) : IVariable.empty();
		}
		if (key.equals("output")) {
			return IVariable.from(this.recipe.getResultItem(registries), registries);
		} else if (key.equals("fluid")) {
			return IVariable.wrap(ModUtil.getRegistryName(this.recipe.getInputFluid().getFluid()).toString(), registries);
		} else if (key.equals("fluidAmount")) {
			return IVariable.wrap(this.recipe.getInputFluid().getAmount(), registries);
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
			return IVariable.from(ingredient.getItems(), registries);
		} else {
			return IVariable.empty();
		}
	}
}
