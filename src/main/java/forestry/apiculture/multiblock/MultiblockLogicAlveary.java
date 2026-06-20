package forestry.apiculture.multiblock;

import forestry.api.multiblock.IMultiblockLogicAlveary;
import forestry.core.multiblock.MultiblockController;
import forestry.core.multiblock.MultiblockLogicBase;

public class MultiblockLogicAlveary extends MultiblockLogicBase implements IMultiblockLogicAlveary {
	public MultiblockLogicAlveary() {
	}

	@Override
	public IAlvearyControllerInternal getController() {
		MultiblockController controller = resolveController();
		if (controller instanceof IAlvearyControllerInternal internal && controller.isAssembled()) {
			return internal;
		}
		return FakeAlvearyController.INSTANCE;
	}
}
