package forestry.cultivation.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import forestry.api.farming.HorizontalDirection;
import forestry.api.farming.IFarmable;
import forestry.cultivation.tiles.TilePlanter;
import forestry.farming.FarmHelper;
import forestry.farming.multiblock.IFarmInventoryInternal;
import forestry.farming.multiblock.InventoryPlantation;

public class InventoryPlanter extends InventoryPlantation<TilePlanter> implements IFarmInventoryInternal {
	public static InventoryPlantation.InventoryConfig CONFIG = new InventoryPlantation.InventoryConfig(
			0, 4,
			4, 4,
			8, 4,
			12, 1,
			13, 1
	);

	public InventoryPlanter(TilePlanter housing) {
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
		ItemStack germlingStack = germlingsInventory.getItem(index);
		if (germlingStack.isEmpty() || !germling.isGermling(germlingStack)) {
			return false;
		}

		if (germling.plantSaplingAt(player, germlingStack, player.level(), pos)) {
			germlingsInventory.removeItem(index, 1);
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
