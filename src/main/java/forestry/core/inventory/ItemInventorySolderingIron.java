package forestry.core.inventory;

import com.google.common.collect.ImmutableSet;
import forestry.api.IForestryApi;
import forestry.api.circuits.CircuitLayout;
import forestry.api.circuits.ICircuit;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;
import forestry.api.core.IErrorSource;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.circuits.ItemCircuitBoard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemInventorySolderingIron extends ItemInventory implements IErrorSource {
	private static final short INPUT_SLOT = 0;
	private static final short OUTPUT_SLOT = 1;
	private static final short INGREDIENT_SLOT_START = 2;
	private static final short INGREDIENT_SLOT_COUNT = 4;

	private int layoutIndex;

	public ItemInventorySolderingIron(Player player, ItemStack stack) {
		super(6, stack);

		this.layoutIndex = 0;
	}

	private static List<CircuitLayout> layouts() {
		return IForestryApi.INSTANCE.getCircuitManager().getLayouts();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	public CircuitLayout getLayout() {
		return layouts().get(this.layoutIndex);
	}

	public void setLayout(CircuitLayout layout) {
		this.layoutIndex = Math.max(0, layouts().indexOf(layout));
	}

	public void advanceLayout() {
		int layoutCount = layouts().size();
		this.layoutIndex = (this.layoutIndex + 1) % layoutCount;
	}

	public void regressLayout() {
		if (this.layoutIndex == 0) {
			int layoutCount = layouts().size();
			this.layoutIndex = layoutCount - 1;
		} else {
			this.layoutIndex--;
		}
	}

	private ICircuit[] getCircuits(boolean doConsume) {
		ICircuit[] circuits = new ICircuit[INGREDIENT_SLOT_COUNT];

		for (short i = 0; i < INGREDIENT_SLOT_COUNT; i++) {
			ItemStack ingredient = getItem(INGREDIENT_SLOT_START + i);
			if (!ingredient.isEmpty()) {
				ICircuit circuit = IForestryApi.INSTANCE.getCircuitManager().getCircuit(layouts().get(this.layoutIndex), ingredient);

				if (circuit != null) {
					if (doConsume) {
						removeItem(INGREDIENT_SLOT_START + i, ingredient.getCount());
					}
					circuits[i] = circuit;
				}
			}
		}

		return circuits;
	}

	@Override
	public void onSlotClick(int slotIndex, Player player) {
		List<CircuitLayout> layouts = layouts();
		if (layouts.get(this.layoutIndex) == null) {
			return;
		}

		ItemStack inputCircuitBoard = getItem(INPUT_SLOT);

		if (inputCircuitBoard.isEmpty() || inputCircuitBoard.getCount() > 1) {
			return;
		}
		if (!getItem(OUTPUT_SLOT).isEmpty()) {
			return;
		}

		// Need a chipset item
		if (!IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(inputCircuitBoard)) {
			return;
		}

		Item item = inputCircuitBoard.getItem();
		if (!(item instanceof ItemCircuitBoard circuitBoard)) {
			return;
		}

		EnumCircuitBoardType type = circuitBoard.getType();
		if (getCircuitCount() != type.getSockets()) {
			return;
		}

		ICircuit[] circuits = getCircuits(true);

		ItemStack outputCircuitBoard = ItemCircuitBoard.createCircuitboard(type, layouts.get(this.layoutIndex), circuits);

		setItem(OUTPUT_SLOT, outputCircuitBoard);
		setItem(INPUT_SLOT, ItemStack.EMPTY);
	}

	private int getCircuitCount() {
		ICircuit[] circuits = getCircuits(false);
		int count = 0;
		for (ICircuit circuit : circuits) {
			if (circuit != null) {
				count++;
			}
		}
		return count;
	}

	@Override
	public ImmutableSet<IError> getErrors() {
		ImmutableSet.Builder<IError> errorStates = ImmutableSet.builder();

		if (layouts().get(this.layoutIndex) == null) {
			errorStates.add(ForestryError.NO_CIRCUIT_LAYOUT);
		}

		ItemStack blankCircuitBoard = getItem(INPUT_SLOT);

		if (blankCircuitBoard.isEmpty()) {
			errorStates.add(ForestryError.NO_CIRCUIT_BOARD);
		} else {
			Item item = blankCircuitBoard.getItem();
			if (!(item instanceof ItemCircuitBoard)) {
				return errorStates.build();
			}
			EnumCircuitBoardType type = ((ItemCircuitBoard) item).getType();

			int circuitCount = 0;
			for (short i = 0; i < type.getSockets(); i++) {
				if (!getItem(INGREDIENT_SLOT_START + i).isEmpty()) {
					circuitCount++;
				}
			}

			if (circuitCount != type.getSockets()) {
				errorStates.add(ForestryError.CIRCUIT_MISMATCH);
			} else {
				int count = getCircuitCount();
				if (count != type.getSockets()) {
					errorStates.add(ForestryError.NO_CIRCUIT_LAYOUT);
				}
			}
		}

		return errorStates.build();
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		Item item = stack.getItem();
		if (slotIndex == INPUT_SLOT) {
			return item instanceof ItemCircuitBoard;
		} else if (slotIndex >= INGREDIENT_SLOT_START && slotIndex < INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT) {
			return IForestryApi.INSTANCE.getCircuitManager().getCircuit(layouts().get(this.layoutIndex), stack) != null;
		}
		return false;
	}
}
