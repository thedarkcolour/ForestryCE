package forestry.factory.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

class FakeCraftingInventory {
	private static final AbstractContainerMenu EMPTY_CONTAINER = new AbstractContainerMenu(null, -1) {
		@Override
		public ItemStack quickMoveStack(Player player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player playerIn) {
			return true;
		}
	};

	public static CraftingInput of(Container backing) {
		CraftingContainer inventory = new TransientCraftingContainer(EMPTY_CONTAINER, 3, 3);

		for (int i = 0; i < 9; i++) {
			inventory.setItem(i, backing.getItem(i));
		}

		return inventory.asCraftInput();
	}
}
