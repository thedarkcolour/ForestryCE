package forestry.core.inventory.watchers;

import net.minecraft.world.Container;

public enum FakeSlotChangeWatcher implements ISlotChangeWatcher {
	INSTANCE;

	@Override
	public void onSlotChanged(Container inventory, int slot) {
	}
}
