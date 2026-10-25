package forestry.core.platform.inventory;

import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.core.content.analyzer.AnalyzerBlockEntity;
import forestry.core.platform.util.GeneticsUtil;
import forestry.core.platform.util.SlotUtil;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Optional;

// inventory for block form analyzer
public class AnalyzerInventory extends BlockEntityInventoryAdapter<AnalyzerBlockEntity> {
	public static final short SLOT_ANALYZE = 0;
	public static final short SLOT_CAN = 1;
	public static final short SLOT_INPUT_1 = 2;
	public static final short SLOT_INPUT_COUNT = 6;
	public static final short SLOT_OUTPUT_1 = 8;
	public static final short SLOT_OUTPUT_COUNT = 4;

	public AnalyzerInventory(AnalyzerBlockEntity analyzer) {
		super(analyzer, 12, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (SlotUtil.isSlotInRange(slotIndex, SLOT_INPUT_1, SLOT_INPUT_COUNT)) {
			return IIndividualHandlerItem.isIndividual(stack);
		} else if (slotIndex == SLOT_CAN) {
			Optional<FluidStack> fluid = FluidUtil.getFluidContained(stack);
			return fluid.map(f -> this.tile.getTankManager().canFillFluidType(f)).orElse(false);
		}

		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_OUTPUT_1, SLOT_OUTPUT_COUNT);
	}

	@Override
	public void setItem(int slotId, ItemStack itemStack) {
		if (!SpeciesUtil.TREE_TYPE.get().isMember(itemStack)) {
			itemStack = GeneticsUtil.convertToGeneticEquivalent(itemStack);
		}

		super.setItem(slotId, itemStack);
	}
}
