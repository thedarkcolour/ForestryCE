package forestry.core.items;

import forestry.core.circuits.ContainerSolderingIron;
import forestry.core.circuits.ISolderingIron;
import forestry.core.inventory.ItemInventorySolderingIron;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemSolderingIron extends ItemWithGui implements ISolderingIron {
	public ItemSolderingIron() {
		super(new Item.Properties().durability(5));
	}

	@Override
	public AbstractContainerMenu getContainer(int windowId, Player player, ItemStack heldItem) {
		return new ContainerSolderingIron(windowId, player, new ItemInventorySolderingIron(player, heldItem));
	}
}
