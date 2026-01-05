package forestry.core.inventory;

import com.google.common.collect.ImmutableSet;
import forestry.api.IForestryApi;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitLayout;
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
	private final List<ICircuitLayout> layouts = IForestryApi.INSTANCE.getCircuitManager().getLayouts();
	private final int layoutCount = this.layouts.size();
	private int layoutIndex;

	private static final short inputCircuitBoardSlot = 0;
	private static final short finishedCircuitBoardSlot = 1;
	private static final short ingredientSlot1 = 2;
	private static final short ingredientSlotCount = 4;

	public ItemInventorySolderingIron(Player player, ItemStack itemStack) {
		super(player, 6, itemStack);

		this.layoutIndex = 0;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	public ICircuitLayout getLayout() {
		return this.layouts.get(this.layoutIndex);
	}

	public void setLayout(ICircuitLayout layout) {
		this.layoutIndex = Math.max(0, this.layouts.indexOf(layout));
	}

	public void advanceLayout() {
		this.layoutIndex = (this.layoutIndex + 1) % this.layoutCount;
	}

	public void regressLayout() {
		if (this.layoutIndex == 0) {
			this.layoutIndex = this.layoutCount - 1;
		} else {
			this.layoutIndex--;
		}
	}

	private ICircuit[] getCircuits(boolean doConsume) {
		ICircuit[] circuits = new ICircuit[ingredientSlotCount];

		for (short i = 0; i < ingredientSlotCount; i++) {
			ItemStack ingredient = getItem(ingredientSlot1 + i);
			if (!ingredient.isEmpty()) {
				ICircuit circuit = IForestryApi.INSTANCE.getCircuitManager().getCircuit(this.layouts.get(this.layoutIndex), ingredient);

				if (circuit != null) {
					if (doConsume) {
						removeItem(ingredientSlot1 + i, ingredient.getCount());
					}
					circuits[i] = circuit;
				}
			}
		}

		return circuits;
	}

	@Override
	public void onSlotClick(int slotIndex, Player player) {
		if (this.layouts.get(this.layoutIndex) == null) {
			return;
		}

		ItemStack inputCircuitBoard = getItem(inputCircuitBoardSlot);

		if (inputCircuitBoard.isEmpty() || inputCircuitBoard.getCount() > 1) {
			return;
		}
		if (!getItem(finishedCircuitBoardSlot).isEmpty()) {
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

		ItemStack outputCircuitBoard = ItemCircuitBoard.createCircuitboard(type, this.layouts.get(this.layoutIndex), circuits);

		setItem(finishedCircuitBoardSlot, outputCircuitBoard);
		setItem(inputCircuitBoardSlot, ItemStack.EMPTY);
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

		if (this.layouts.get(this.layoutIndex) == null) {
			errorStates.add(ForestryError.NO_CIRCUIT_LAYOUT);
		}

		ItemStack blankCircuitBoard = getItem(inputCircuitBoardSlot);

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
				if (!getItem(ingredientSlot1 + i).isEmpty()) {
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
		if (slotIndex == inputCircuitBoardSlot) {
			return item instanceof ItemCircuitBoard;
		} else if (slotIndex >= ingredientSlot1 && slotIndex < ingredientSlot1 + ingredientSlotCount) {
			return IForestryApi.INSTANCE.getCircuitManager().getCircuit(this.layouts.get(this.layoutIndex), stack) != null;
		}
		return false;
	}
}
