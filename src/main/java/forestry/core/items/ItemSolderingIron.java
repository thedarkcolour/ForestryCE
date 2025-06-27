package forestry.core.items;

import forestry.core.circuits.SolderingIronMenu;
import forestry.core.inventory.ItemInventorySolderingIron;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;

public class ItemSolderingIron extends ItemWithGui {
	public ItemSolderingIron() {
		super(new Item.Properties().durability(5));
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, int slotIndex) {
		return new SolderingIronMenu(containerId, playerInv, new ItemInventorySolderingIron(slotIndex));
	}
}
