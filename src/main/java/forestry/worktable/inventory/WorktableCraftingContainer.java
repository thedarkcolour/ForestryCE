package forestry.worktable.inventory;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;

import forestry.core.gui.DummyMenu;

public class WorktableCraftingContainer extends TransientCraftingContainer {
	private final AbstractContainerMenu menu;

	public WorktableCraftingContainer(AbstractContainerMenu menu) {
		super(menu, 3, 3);
		this.menu = menu;
	}

	public WorktableCraftingContainer() {
		this(DummyMenu.INSTANCE);
	}

	public WorktableCraftingContainer copy() {
		WorktableCraftingContainer copy = new WorktableCraftingContainer(menu);
		for (int slot = 0; slot < getContainerSize(); slot++) {
			copy.setItem(slot, getItem(slot).copy());
		}
		return copy;
	}
}
