package forestry.apiculture.alveary;

import forestry.apiculture.alveary.multiblock.AlvearyHygroregulatorBlockEntity;
import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Optional;

public class AlvearyHygroregulatorInventory extends BlockEntityInventoryAdapter<AlvearyHygroregulatorBlockEntity> {
	public static final short SLOT_INPUT = 0;

	public AlvearyHygroregulatorInventory(AlvearyHygroregulatorBlockEntity alvearyHygroregulator) {
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
