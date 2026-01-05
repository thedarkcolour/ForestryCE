package forestry.core.gui;

import net.minecraft.world.Container;

public interface IContainerCrafting {
	void onCraftMatrixChanged(Container iinventory, int slot);
}
