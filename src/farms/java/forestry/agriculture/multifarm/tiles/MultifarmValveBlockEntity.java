package forestry.agriculture.multifarm.tiles;

import forestry.core.platform.fluids.ITankManager;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.agriculture.features.MultifarmBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MultifarmValveBlockEntity extends MultifarmBlockEntity implements ILiquidTankTile {
	public MultifarmValveBlockEntity(BlockPos pos, BlockState state) {
		super(MultifarmBlockEntities.VALVE.tileType(), pos, state);
	}

	@Override
	public ITankManager getTankManager() {
		return getMultiblockLogic().getController().getTankManager();
	}

	public IFluidHandler getFluidHandler(net.minecraft.core.Direction facing) {
		return getTankManager();
	}
}
