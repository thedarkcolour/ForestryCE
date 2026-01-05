package forestry.arboriculture.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

public interface ITreeBlockType {
	void setDirection(Direction facing);

	boolean setBlock(LevelAccessor level, BlockPos pos);
}
