package forestry.apiculture.inventory;

import forestry.apiculture.multiblock.TileAlvearyHygroregulator;
import forestry.core.inventory.InventoryAdapterTile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Optional;

public class InventoryHygroregulator extends InventoryAdapterTile<TileAlvearyHygroregulator> {
	public static final short SLOT_INPUT = 0;

	public InventoryHygroregulator(TileAlvearyHygroregulator alvearyHygroregulator) {
		super(alvearyHygroregulator, 1, "CanInv");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_INPUT) {
			Optional<FluidStack> fluidCap = FluidUtil.getFluidContained(stack);
			return fluidCap.map(f -> this.tile.getTankManager().canFillFluidType(f)).orElse(false);
		}
		return false;
	}
}
