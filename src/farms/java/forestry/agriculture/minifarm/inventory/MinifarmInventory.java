package forestry.agriculture.minifarm.inventory;

import forestry.api.agriculture.HorizontalDirection;
import forestry.api.agriculture.IFarmable;
import forestry.agriculture.minifarm.tiles.AbstractMinifarmBlockEntity;
import forestry.agriculture.multifarm.multiblock.IFarmInventoryInternal;
import forestry.agriculture.multifarm.multiblock.AbstractFarmInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MinifarmInventory extends AbstractFarmInventory<AbstractMinifarmBlockEntity> implements IFarmInventoryInternal {
	public static AbstractFarmInventory.InventoryConfig CONFIG = new AbstractFarmInventory.InventoryConfig(
		0, 4,
		4, 4,
		8, 4,
		12, 1,
		13, 1
	);

	public MinifarmInventory(AbstractMinifarmBlockEntity housing) {
		super(housing, CONFIG);
	}

	@Override
	public boolean plantGermling(IFarmable germling, Player player, BlockPos pos) {
		for (Direction direction : HorizontalDirection.VALUES) {
			if (plantGermling(germling, player, pos, direction)) {
				return true;
			}
		}
		return false;
	}

	public boolean plantGermling(IFarmable germling, Player player, BlockPos pos, Direction direction) {
		int index = getSlotIndex(direction.getOpposite());
		ItemStack germlingStack = this.germlingsInventory.getItem(index);
		if (germlingStack.isEmpty() || !germling.isGermling(germlingStack)) {
			return false;
		}

		if (germling.plantSaplingAt(player, germlingStack, player.level(), pos)) {
            this.germlingsInventory.removeItem(index, 1);
			return true;
		}
		return false;
	}

	private static int getSlotIndex(Direction direction) {
		return switch (direction) {
			case NORTH -> 0;
			case EAST -> 1;
			case SOUTH -> 2;
			default -> 3;
		};
	}
}
