package forestry.mail.items;

import forestry.core.items.ItemWithGui;
import forestry.mail.gui.ContainerCatalogue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemCatalogue extends ItemWithGui {
	public ItemCatalogue() {
		super(new Item.Properties());
	}

	@Nullable
	@Override
	public AbstractContainerMenu getContainer(int windowId, Player player, ItemStack heldItem) {
		return new ContainerCatalogue(windowId, player.getInventory());
	}
}
