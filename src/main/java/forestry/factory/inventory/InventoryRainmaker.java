package forestry.factory.inventory;

import forestry.api.fuels.FuelManager;
import forestry.api.fuels.RainSubstrate;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.factory.tiles.TileMillRainmaker;
import net.minecraft.world.item.ItemStack;

public class InventoryRainmaker extends InventoryAdapterTile<TileMillRainmaker> {
	private static final int SLOT_SUBSTRATE = 0;

	public InventoryRainmaker(TileMillRainmaker tile) {
		super(tile, 1, "items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_SUBSTRATE) {
			if (FuelManager.rainSubstrate.containsKey(stack) && this.tile.charge == 0 && this.tile.progress == 0) {
				RainSubstrate substrate = FuelManager.rainSubstrate.get(stack);
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
			RainSubstrate substrate = FuelManager.rainSubstrate.get(itemStack);
			if (substrate != null && ItemStack.isSameItem(substrate.item(), itemStack)) {
                this.tile.addCharge(substrate);
			}
		}
	}
}
