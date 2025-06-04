package forestry.api.circuits;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Manages data about different circuits and layouts in Forestry.
 * Register data with {@link forestry.api.plugin.IForestryPlugin#registerCircuits}.
 */
public interface ICircuitManager {
	/**
	 * @return A collection of all registered circuit layouts.
	 */
	List<CircuitLayout> getLayouts();

	/**
	 * Retrieves the item's circuit for the given circuit layout, or null if this item is not applicable for the layout.
	 *
	 * @param layout The circuit layout.
	 * @param stack  The item (usually an electron tube).
	 * @return The circuit associated with the circuit layout and item, or {@code null} if this item doesn't work for the circuit layout.
	 */
	@Nullable
	ICircuit getCircuit(CircuitLayout layout, ItemStack stack);

	/**
	 * @return The circuit layout with the given ID, or {@code null} if none was registered with that ID.
	 */
	@Nullable
	CircuitLayout getLayout(String layoutId);

	/**
	 * Note: Currently hardcoded to Forestry circuit board.
	 *
	 * @return {@code true} if the given item is a circuit board and can have circuits installed in it.
	 */
	boolean isCircuitBoard(ItemStack stack);

	Collection<CircuitHolder> getCircuitHolders();
}
