package forestry.core.content.machines.recipes.jei.fabricator;

import forestry.api.core.machines.IFabricatorRecipe;
import forestry.core.platform.recipes.jei.ForestryRecipeType;
import forestry.core.platform.util.JeiUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.content.machines.features.FactoryMenuTypes;
import forestry.core.content.machines.gui.FabricatorMenu;
import forestry.core.content.machines.network.packets.PacketRecipeTransferRequest;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class FabricatorRecipeTransferHandler implements IRecipeTransferHandler<FabricatorMenu, IFabricatorRecipe> {
	@Override
	public Class<FabricatorMenu> getContainerClass() {
		return FabricatorMenu.class;
	}

	@Override
	public Optional<MenuType<FabricatorMenu>> getMenuType() {
		return Optional.of(FactoryMenuTypes.FABRICATOR.menuType());
	}

	@Override
	public RecipeType<IFabricatorRecipe> getRecipeType() {
		return ForestryRecipeType.FABRICATOR;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(FabricatorMenu menu, IFabricatorRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (doTransfer) {
			Container craftingInventory = menu.getTile().getCraftingInventory();
			NonNullList<ItemStack> items = JeiUtil.getFirstItemStacks(recipeSlots);
			for (int i = 0; i < items.size(); i++) {
				craftingInventory.setItem(i, items.get(i));
			}

			NetworkUtil.sendToServer(new PacketRecipeTransferRequest(menu.getTile().getBlockPos(), items));
		}

		return null;
	}
}
