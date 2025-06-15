package forestry.farming.logic;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import forestry.core.features.CoreBlocks;
import forestry.farming.logic.crops.CropPeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Collection;

public class FarmLogicPeat extends FarmLogicWatered {
	public FarmLogicPeat(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public Collection<ICrop> harvest(Level level, IFarmHousing housing, Direction direction, int extent, BlockPos pos) {
		ArrayDeque<ICrop> crops = new ArrayDeque<>();
		for (int i = 0; i < extent; i++) {
			BlockPos position = translateWithOffset(pos, direction, i);
			if (!level.hasChunkAt(position)) {
				return crops;
			}
			BlockState blockState = level.getBlockState(position);
			if (CoreBlocks.PEAT.blockEqual(blockState)) {
				crops.addFirst(new CropPeat(level, position));
			}
		}
		return crops;
	}
}
