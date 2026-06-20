package forestry.farming.multiblock;

import forestry.api.multiblock.IMultiblockLogicFarm;
import forestry.core.multiblock.MultiblockController;
import forestry.core.multiblock.MultiblockLogicBase;

public class MultiblockLogicFarm extends MultiblockLogicBase implements IMultiblockLogicFarm {
	public MultiblockLogicFarm() {
	}

	@Override
	public IFarmControllerInternal getController() {
		MultiblockController controller = resolveController();
		if (controller instanceof IFarmControllerInternal internal && controller.isAssembled()) {
			return internal;
		}
		return FakeFarmController.INSTANCE;
	}
}
