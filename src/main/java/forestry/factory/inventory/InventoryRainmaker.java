package forestry.factory.inventory;

import forestry.api.recipes.IRainSubstrate;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.utils.RecipeUtils;
import forestry.factory.tiles.TileMillRainmaker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public class InventoryRainmaker extends InventoryAdapterTile<TileMillRainmaker> {
	private static final int SLOT_SUBSTRATE = 0;

	public InventoryRainmaker(TileMillRainmaker tile) {
		super(tile, 1, "items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_SUBSTRATE && this.tile.charge == 0 && this.tile.progress == 0) {
			IRainSubstrate substrate = getSubstrate(stack);
			if (substrate != null) {
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
			IRainSubstrate substrate = getSubstrate(itemStack);
			if (substrate != null) {
                this.tile.addCharge(substrate);
			}
		}
	}

	private static IRainSubstrate getSubstrate(ItemStack stack) {
		RecipeManager manager = RecipeUtils.getRecipeManager();
		return manager == null ? null : RecipeUtils.getRainSubstrate(manager, stack);
	}
}
