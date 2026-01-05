package forestry.farming.multiblock;

import forestry.api.multiblock.IMultiblockLogicFarm;
import forestry.core.multiblock.MultiblockLogic;
import net.minecraft.world.level.Level;

public class MultiblockLogicFarm extends MultiblockLogic<IFarmControllerInternal> implements IMultiblockLogicFarm {
	public MultiblockLogicFarm() {
		super(IFarmControllerInternal.class);
	}

	@Override
	public IFarmControllerInternal getController() {
		if (this.controller != null) {
			return this.controller;
		} else {
			return FakeFarmController.INSTANCE;
		}
	}

	@Override
	public IFarmControllerInternal createNewController(Level level) {
		return new FarmController(level);
	}
}
