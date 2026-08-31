package forestry.core.content.machines.inventory;

import forestry.core.platform.inventory.InventoryAdapterTile;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.platform.util.SlotUtil;
import forestry.core.content.machines.tiles.TileMoistener;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Optional;
import forestry.api.ForestryDataMaps;

public class InventoryMoistener extends InventoryAdapterTile<TileMoistener> {
	public static final short SLOT_STASH_1 = 0;
	public static final short SLOT_STASH_COUNT = 6;
	public static final short SLOT_RESERVOIR_1 = 6;
	public static final short SLOT_RESERVOIR_COUNT = 3;
	public static final short SLOT_WORKING = 9;
	public static final short SLOT_PRODUCT = 10;
	public static final short SLOT_RESOURCE = 11;

	public InventoryMoistener(TileMoistener moistener) {
		super(moistener, 12, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_RESOURCE) {
			return RecipeUtils.getMoistenerRecipe(this.tile.getLevel().getRecipeManager(), stack) != null;
		}

		if (SlotUtil.isSlotInRange(slotIndex, SLOT_STASH_1, SLOT_STASH_COUNT)) {
			return stack.getItemHolder().getData(ForestryDataMaps.MOISTENER_FUELS) != null;
		}

		if (slotIndex == SLOT_PRODUCT) {
			Optional<FluidStack> fluidCap = FluidUtil.getFluidContained(stack);
			return fluidCap.map(f -> this.tile.getTankManager().canFillFluidType(f)).orElse(false);    //TODO very common pattern. Create Helper?
		}

		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		if (slotIndex == SLOT_PRODUCT) {
			return true;
		}

		if (SlotUtil.isSlotInRange(slotIndex, SLOT_STASH_1, SLOT_STASH_COUNT + SLOT_RESERVOIR_COUNT)) {
			return itemstack.getItemHolder().getData(ForestryDataMaps.MOISTENER_FUELS) == null;
		}

		return false;
	}
}
