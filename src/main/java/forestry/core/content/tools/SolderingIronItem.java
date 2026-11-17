package forestry.core.content.tools;

import forestry.core.engine.circuits.SolderingIronMenu;
import forestry.core.engine.circuits.ISolderingIron;
import forestry.core.platform.inventory.SolderingIronInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import forestry.core.platform.item.WithScreenItem;

public class SolderingIronItem extends WithScreenItem implements ISolderingIron {
	public SolderingIronItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public AbstractContainerMenu getContainer(int containerId, Player player, ItemStack heldItem) {
		return new SolderingIronMenu(containerId, player, new SolderingIronInventory(player, heldItem));
	}
}
