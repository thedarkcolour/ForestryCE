package forestry.farming.multiblock;

import forestry.api.farming.IFarmable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Inventory of the farm multiblock.
 */
public class InventoryFarm extends InventoryPlantation<FarmController> implements IFarmInventoryInternal {
	public static InventoryPlantation.InventoryConfig CONFIG = new InventoryPlantation.InventoryConfig(
		0, 6,
		6, 6,
		12, 8,
		20, 1,
		21, 1
	);

	public InventoryFarm(FarmController farmController) {
		super(farmController, CONFIG);
	}

	@Override
	public boolean plantGermling(IFarmable germling, Player player, BlockPos pos) {
		for (int i = 0; i < this.germlingsInventory.getContainerSize(); i++) {
			ItemStack germlingStack = this.germlingsInventory.getItem(i);
			if (germlingStack.isEmpty() || !germling.isGermling(germlingStack)) {
				continue;
			}

			if (germling.plantSaplingAt(player, germlingStack, player.level(), pos)) {
                this.germlingsInventory.removeItem(i, 1);
				return true;
			}
		}
		return false;
	}

}
