package forestry.core.content.energy.inventory;

import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.content.energy.tiles.CombustionEngineBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Optional;

public class CombustionEngineInventory extends BlockEntityInventoryAdapter<CombustionEngineBlockEntity> {
	public static final short SLOT_CAN = 0;

	public CombustionEngineInventory(CombustionEngineBlockEntity engineIron) {
		super(engineIron, 1, "Items");
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return true;
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_CAN) {
			Optional<FluidStack> fluid = FluidUtil.getFluidContained(stack);
			return fluid.map(f -> this.tile.getTankManager().canFillFluidType(f)).orElse(false);
		}

		return false;
	}
}
