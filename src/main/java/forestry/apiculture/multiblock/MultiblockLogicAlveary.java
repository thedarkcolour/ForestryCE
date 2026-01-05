package forestry.apiculture.multiblock;

import forestry.api.multiblock.IMultiblockLogicAlveary;
import forestry.core.multiblock.MultiblockLogic;
import net.minecraft.world.level.Level;

public class MultiblockLogicAlveary extends MultiblockLogic<IAlvearyControllerInternal> implements IMultiblockLogicAlveary {
	public MultiblockLogicAlveary() {
		super(IAlvearyControllerInternal.class);
	}

	@Override
	public IAlvearyControllerInternal getController() {
		if (super.isConnected()) {
			return this.controller;
		} else {
			return FakeAlvearyController.INSTANCE;
		}
	}

	@Override
	public IAlvearyControllerInternal createNewController(Level level) {
		return new AlvearyController(level);
	}

	@Override
	public void becomeMultiblockSaveDelegate() {
		super.becomeMultiblockSaveDelegate();
	}

	@Override
	public void forfeitMultiblockSaveDelegate() {
		super.forfeitMultiblockSaveDelegate();
	}
}
