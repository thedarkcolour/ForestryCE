package forestry.factory.recipes.jei.carpenter;

import forestry.api.modules.IForestryPacketServer;
import forestry.api.recipes.ICarpenterRecipe;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.compat.jei.JeiUtil;
import forestry.factory.features.FactoryMenuTypes;
import forestry.factory.gui.CarpenterMenu;
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

public class CarpenterRecipeTransferHandler implements IRecipeTransferHandler<CarpenterMenu, ICarpenterRecipe> {
	@Override
	public Class<CarpenterMenu> getContainerClass() {
		return CarpenterMenu.class;
	}

	@Override
	public Optional<MenuType<CarpenterMenu>> getMenuType() {
		return Optional.of(FactoryMenuTypes.CARPENTER.menuType());
	}

	@Override
	public RecipeType<ICarpenterRecipe> getRecipeType() {
		return ForestryRecipeType.CARPENTER;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(CarpenterMenu container, ICarpenterRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (doTransfer) {
			Container craftingInventory = container.getCarpenter().getCraftingInventory();
			NonNullList<ItemStack> items = JeiUtil.getFirstItemStacks(recipeSlots);
			int size = Math.min(9, items.size());
			for (int i = 0; i < size; i++) {
				craftingInventory.setItem(i, items.get(i));
			}
            IForestryPacketServer packet = new PacketRecipeTransferRequest(container.getCarpenter().getBlockPos(), items);
            PacketDistributor.sendToServer(packet);
        }

		return null;
	}
}
