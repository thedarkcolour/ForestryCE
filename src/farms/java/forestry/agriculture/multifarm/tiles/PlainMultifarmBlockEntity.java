package forestry.agriculture.multifarm.tiles;

import forestry.agriculture.features.FarmingBlockEntities;
import forestry.agriculture.multifarm.multiblock.FarmPattern;
import forestry.api.core.multiblock.IMultiblockController;
import forestry.agriculture.multifarm.blocks.MultifarmBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlainMultifarmBlockEntity extends MultifarmBlockEntity {
	public PlainMultifarmBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.PLAIN.tileType(), pos, state);
	}

	@Override
	public void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord) {
		super.onMachineAssembled(multiblockController, minCoord, maxCoord);

		// set band block meta
		int bandY = maxCoord.getY() - 1;
		if (getBlockPos().getY() == bandY) {
			BlockState state = getBlockState();
			this.level.setBlock(this.worldPosition, state.setValue(MultifarmBlock.BAND, true), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();

		// set band block meta back to normal
		BlockState state = getBlockState();
		this.level.setBlock(this.worldPosition, state.setValue(MultifarmBlock.BAND, false), Block.UPDATE_CLIENTS);
	}

	@Override
	public String patternTypeId() {
		return FarmPattern.PLAIN;
	}

}
