package forestry.core.circuits;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import forestry.api.circuits.CircuitHolder;
import forestry.api.circuits.CircuitLayout;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitManager;
import forestry.core.features.CoreItems;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class CircuitManager implements ICircuitManager {
	private final ImmutableMultimap<CircuitLayout, CircuitHolder> circuitHolders;
	private final ImmutableMap<String, CircuitLayout> layoutsById;

	public CircuitManager(ImmutableMultimap<CircuitLayout, CircuitHolder> circuitHolders, ImmutableMap<String, CircuitLayout> layoutsById) {
		this.circuitHolders = circuitHolders;
		this.layoutsById = layoutsById;
	}

	@Override
	public List<CircuitLayout> getLayouts() {
		return this.layoutsById.values().asList();
	}

	@Nullable
	@Override
	public ICircuit getCircuit(CircuitLayout layout, ItemStack stack) {
		for (CircuitHolder holder : this.circuitHolders.get(layout)) {
			if (ItemStack.isSameItem(holder.stack(), stack)) {
				return holder.circuit();
			}
		}
		return null;
	}

	@Nullable
	@Override
	public CircuitLayout getLayout(String layoutId) {
		return this.layoutsById.get(layoutId);
	}

	@Override
	public boolean isCircuitBoard(ItemStack stack) {
		return CoreItems.CIRCUITBOARDS.itemEqual(stack);
	}

	@Override
	public Collection<CircuitHolder> getCircuitHolders() {
		return this.circuitHolders.values();
	}
}
