package forestry.core.content.machines.inventory;

import forestry.core.content.machines.tiles.SmelterBlockEntity;
import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.platform.inventory.wrappers.InventoryMapper;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.SlotUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmelterInventory extends BlockEntityInventoryAdapter<SmelterBlockEntity> {
	public static final int SLOT_INPUT_1 = 0;
	public static final int SLOT_INPUT_COUNT = 9;
	public static final int SLOT_OUTPUT = 10;

	public SmelterInventory(SmelterBlockEntity smelter) {
		super(smelter, 11, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_INPUT_1, SLOT_INPUT_COUNT);
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return slotIndex == SLOT_OUTPUT;
	}

	public boolean hasResources() {
		return !InventoryUtil.isEmpty(this, SLOT_INPUT_1, SLOT_INPUT_COUNT);
	}

	public List<ItemStack> getResources() {
		return InventoryUtil.getStacks(this, SLOT_INPUT_1, SLOT_INPUT_COUNT);
	}

	public boolean addResult(ItemStack product, boolean doAdd) {
		return InventoryUtil.tryAddStack(this, product, SLOT_OUTPUT, 1, true, doAdd);
	}

	// Deviation from 1.20.1: the loop below stopped one slot short of the last input slot, and the
	// partial-slot branch recorded the shortfall instead of what the slot actually holds, so an
	// ingredient spread over several slots was never fully consumed. Both are corrected here
	public boolean removeResources(List<SizedIngredient> inputs) {
		Container inventory = new InventoryMapper(this, SLOT_INPUT_1, SLOT_INPUT_COUNT);

		Map<Integer, Integer> removeAmounts = new HashMap<>();

		// Check we can actually remove all resources
		for (SizedIngredient input : inputs) {
			int amountToRemove = input.count();

			for (int slot = 0; slot < SLOT_INPUT_COUNT; slot++) {
				ItemStack stack = inventory.getItem(slot);
				// Only the ingredient, never SizedIngredient#test, which also demands that this one
				// slot hold the whole count. An input may be spread over several slots
				if (!input.ingredient().test(stack)) {
					continue;
				}

				// What this slot still has after the removals booked so far
				int available = stack.getCount() - removeAmounts.getOrDefault(slot, 0);
				if (available <= 0) {
					continue;
				}

				int taken = Math.min(available, amountToRemove);
				removeAmounts.merge(slot, taken, Integer::sum);
				amountToRemove -= taken;

				if (amountToRemove == 0) {
					break;
				}
			}

			// If we loop through all slots and we still haven't got enough resources, exit
			if (amountToRemove > 0) {
				return false;
			}
		}

		// We have enough resources, now let's actually remove them
		for (Map.Entry<Integer, Integer> entry : removeAmounts.entrySet()) {
			ItemStack oldStack = inventory.getItem(entry.getKey());
			int remaining = oldStack.getCount() - entry.getValue();
			inventory.setItem(entry.getKey(), remaining <= 0 ? ItemStack.EMPTY : oldStack.copyWithCount(remaining));
		}
		return true;
	}
}
