package forestry.core.content.machines.inventory;

import forestry.core.platform.inventory.InventoryAdapterTile;
import forestry.core.content.machines.tiles.TileMillRainmaker;
import net.minecraft.world.item.ItemStack;
import forestry.api.ForestryDataMaps;
import forestry.api.core.machines.fuels.RainmakerSubstrate;

public class InventoryRainmaker extends InventoryAdapterTile<TileMillRainmaker> {
	private static final int SLOT_SUBSTRATE = 0;

	public InventoryRainmaker(TileMillRainmaker tile) {
		super(tile, 1, "items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_SUBSTRATE) {
			RainmakerSubstrate substrate = stack.getItemHolder().getData(ForestryDataMaps.RAINMAKER_FUELS);
			if (substrate != null && this.tile.charge == 0 && this.tile.progress == 0) {
				if (this.tile.getLevel().isRaining() && substrate.reverse()) {
					return true;
				} else {
					return !this.tile.getLevel().isRaining() && !substrate.reverse();
				}
			}
		}

		return false;
	}

	@Override
	public void setItem(int slotIndex, ItemStack itemStack) {
		if (slotIndex == SLOT_SUBSTRATE) {
			RainmakerSubstrate substrate = itemStack.getItemHolder().getData(ForestryDataMaps.RAINMAKER_FUELS);
			if (substrate != null) {
                this.tile.addCharge(substrate);
			}
		}
	}
}
