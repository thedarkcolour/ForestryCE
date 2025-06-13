package forestry.core.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class InventoryAdapterTile<T extends BlockEntity> extends InventoryAdapterRestricted {
	protected final T tile;

	public InventoryAdapterTile(T tile, int size, String name) {
		this(tile, size, name, 64);
	}

	public InventoryAdapterTile(T tile, int size, String name, int stackLimit) {
		super(size, name, stackLimit);
		this.tile = tile;
	}

	@Override
	public void setChanged() {
		super.setChanged();
        this.tile.setChanged();
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return false;
	}
}
