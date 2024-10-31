package forestry.worktable.compat;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import forestry.core.utils.JeiUtil;
import forestry.core.utils.RecipeUtils;
import forestry.worktable.features.WorktableMenus;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.screens.WorktableMenu;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

class WorktableRecipeTransferHandler implements IRecipeTransferHandler<WorktableMenu, CraftingRecipe> {
	@Override
	public Class<WorktableMenu> getContainerClass() {
		return WorktableMenu.class;
	}

	@Override
	public Optional<MenuType<WorktableMenu>> getMenuType() {
		return Optional.of(WorktableMenus.WORKTABLE.menuType());
	}

	@Override
	public RecipeType<CraftingRecipe> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(WorktableMenu container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (doTransfer) {
			CraftingContainer inventory = new TransientCraftingContainer(container, 3, 3);

			NonNullList<ItemStack> firstItemStacks = JeiUtil.getFirstItemStacks(recipeSlots);
			for (int i = 0; i < firstItemStacks.size(); i++) {
				ItemStack firstItemStack = firstItemStacks.get(i);
				inventory.setItem(i, firstItemStack);
			}

			List<CraftingRecipe> matchingRecipes = RecipeUtils.findMatchingRecipes(inventory, player.level());
			if (!matchingRecipes.isEmpty()) {
				MemorizedRecipe memorizedRecipe = new MemorizedRecipe(inventory, matchingRecipes);
				container.sendWorktableRecipeRequest(memorizedRecipe);
			}
		}
		return null;
	}
}
