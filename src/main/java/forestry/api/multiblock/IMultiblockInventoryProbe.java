package forestry.api.multiblock;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Engine-agnostic test/diagnostic seam for the multiblock <b>inventory-conservation</b> regression suite.
 *
 * <p>An {@link IMultiblockController} implementation exposes the machine's <em>shared</em> inventory through this
 * interface so a test harness can assert conservation (items before == items kept + items dropped) across real game
 * operations — chunk unload/reload, controller merges, and block break/replace — <b>without</b> reaching into engine
 * internals. The current ("Erogenous Beef") engine and the upcoming multiblock overhaul must <em>both</em> implement
 * this method; that is precisely what lets the same GameTests run against either engine and so verify the rewrite.
 *
 * <p>This is deliberately the <i>only</i> coupling point between the harness and a specific engine. Everything else the
 * tests do (build a machine, drive it, count drops) goes through vanilla APIs. Keep it minimal: it reports contents,
 * it does not mutate them.
 *
 * @see forestry.api.multiblock.IMultiblockController
 */
public interface IMultiblockInventoryProbe {
	/**
	 * @return a snapshot — independent {@linkplain ItemStack#copy() copies} — of every non-empty stack in this
	 * multiblock's shared inventory at the moment of the call. The list is owned by the caller; mutating it (or the
	 * stacks in it) does not affect the machine. Returns an empty list if the shared inventory is empty.
	 */
	List<ItemStack> snapshotSharedInventory();

	/**
	 * Helper for implementors whose shared inventory is a vanilla {@link Container}: snapshots every non-empty slot.
	 */
	static List<ItemStack> snapshotContainer(Container container) {
		List<ItemStack> snapshot = new ArrayList<>();
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack stack = container.getItem(slot);
			if (!stack.isEmpty()) {
				snapshot.add(stack.copy());
			}
		}
		return snapshot;
	}
}
