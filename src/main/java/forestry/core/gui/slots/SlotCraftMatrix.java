package forestry.core.gui.slots;

import forestry.core.gui.IContainerCrafting;
import net.minecraft.world.Container;

/**
 * Informs the passed container of slot changes. Contains a dummy itemstack.
 */
public class SlotCraftMatrix extends SlotForestry {

	private final IContainerCrafting eventHandler;
	private final int slot;

	public SlotCraftMatrix(IContainerCrafting container, Container iinventory, int i, int j, int k) {
		super(iinventory, i, j, k);
		setPhantom();
		this.eventHandler = container;
		this.slot = i;
		setStackLimit(1);
	}

	@Override
	public void setChanged() {
		super.setChanged();
        this.eventHandler.onCraftMatrixChanged(this.container, this.slot);
	}

}
