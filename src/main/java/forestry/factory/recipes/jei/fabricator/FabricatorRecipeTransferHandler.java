package forestry.factory.recipes.jei.fabricator;

import forestry.api.modules.IForestryPacketServer;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.compat.jei.JeiUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.gui.FabricatorMenu;
import forestry.factory.network.packets.PacketRecipeTransferRequest;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

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
	public IRecipeTransferError transferRecipe(FabricatorMenu container, IFabricatorRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (doTransfer) {
			Container craftingInventory = container.getFabricator().getCraftingInventory();
			NonNullList<ItemStack> items = JeiUtil.getFirstItemStacks(recipeSlots);
			for (int i = 0; i < items.size(); i++) {
				craftingInventory.setItem(i, items.get(i));
			}

            IForestryPacketServer packet = new PacketRecipeTransferRequest(container.getFabricator().getBlockPos(), items);
            PacketDistributor.sendToServer(packet);
        }

		return null;
	}
}
