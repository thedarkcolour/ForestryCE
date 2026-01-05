package forestry.apiculture;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class WaterFlowerType extends FlowerType {
	public WaterFlowerType(TagKey<Block> acceptableFlowers, boolean dominant) {
		super(acceptableFlowers, dominant);
	}

	@Override
	public boolean isPlantablePosition(Level level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() == Blocks.WATER;
	}
}
