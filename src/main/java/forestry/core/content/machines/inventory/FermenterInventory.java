package forestry.core.content.machines.inventory;

import forestry.core.platform.fluids.FluidHelper;
import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.tiles.FermenterBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Optional;
import forestry.api.ForestryDataMaps;

public class FermenterInventory extends BlockEntityInventoryAdapter<FermenterBlockEntity> {
	public static final short SLOT_RESOURCE = 0;
	public static final short SLOT_FUEL = 1;
	public static final short SLOT_CAN_OUTPUT = 2;
	public static final short SLOT_CAN_INPUT = 3;
	public static final short SLOT_INPUT = 4;

	public FermenterInventory(FermenterBlockEntity fermenter) {
		super(fermenter, 5, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_RESOURCE) {
			return RecipeUtils.isFermenterInput(this.tile.getLevel().getRecipeManager(), stack);
		} else if (slotIndex == SLOT_INPUT) {
			Optional<FluidStack> fluid = FluidUtil.getFluidContained(stack);
			return fluid.map(f -> this.tile.getTankManager().canFillFluidType(f)).orElse(false);
		} else if (slotIndex == SLOT_CAN_INPUT) {
			return FluidHelper.isFillableContainerWithRoom(stack);
		} else if (slotIndex == SLOT_FUEL) {
			return stack.getItemHolder().getData(ForestryDataMaps.FERMENTER_FUELS) != null;
		}
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return slotIndex == SLOT_CAN_OUTPUT;
	}
}
