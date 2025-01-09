package forestry.apiculture.hives;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import forestry.api.apiculture.hives.IHiveGen;
import forestry.core.utils.BlockUtil;

public class HiveGenGround implements IHiveGen {
	private final TagKey<Block> blocks;

	public HiveGenGround(TagKey<Block> blocks) {
		this.blocks = blocks;
	}

	@Override
	@Deprecated
	public BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ) {
		// get to the ground
		int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, posX, posZ);
		int minBuildHeight = level.getMinBuildHeight();
		if (groundY == minBuildHeight) {
			return null;
		}

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, groundY, posZ);

		BlockState blockState = level.getBlockState(pos);
		while (IHiveGen.isTreeBlock(blockState) || canReplace(blockState, level, pos)) {
			pos.move(Direction.DOWN);
			if (pos.getY() <= minBuildHeight) {
				return null;
			}
			blockState = level.getBlockState(pos);
		}

		return pos.above();
	}

	@Override
	public boolean canReplace(BlockState blockState, WorldGenLevel world, BlockPos pos) {
		return BlockUtil.canReplace(blockState, world, pos);
	}

	@Override
	public boolean isValidLocation(WorldGenLevel world, BlockPos pos) {
		BlockState groundBlockState = world.getBlockState(pos.below());
		return groundBlockState.is(blocks);
	}
}
