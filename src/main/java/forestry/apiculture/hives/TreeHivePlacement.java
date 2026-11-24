package forestry.apiculture.hives;

import forestry.api.apiculture.hives.IHivePlacement;
import forestry.core.platform.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public enum TreeHivePlacement implements IHivePlacement {
	INSTANCE;

	@Override
	public boolean canReplace(BlockState state, WorldGenLevel level, BlockPos pos) {
		return BlockUtil.canReplace(state, level, pos);
	}

	@Override
	public boolean isValidLocation(WorldGenLevel level, BlockPos pos) {
		BlockPos posAbove = pos.above();
		BlockState blockStateAbove = level.getBlockState(posAbove);
		if (!IHivePlacement.isTreeBlock(blockStateAbove)) {
			return false;
		}

		// not a good location if right on top of something
		BlockPos posBelow = pos.below();
		BlockState blockStateBelow = level.getBlockState(posBelow);
		return canReplace(blockStateBelow, level, posBelow);
	}

	@Override
	@Nullable
	public BlockPos getPosForHive(WorldGenLevel level, RandomSource random, int posX, int posZ) {
		ChunkAccess chunk = level.getChunk(posX >> 4, posZ >> 4);
		Heightmap.Types heightmapType = chunk.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
			? Heightmap.Types.WORLD_SURFACE_WG
			: Heightmap.Types.WORLD_SURFACE;

		int height = chunk.getHeight(heightmapType, posX & 15, posZ & 15);
		int minBuildHeight = chunk.getMinBuildHeight();

		if (height <= minBuildHeight) {
			return null;
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, height, posZ);
		BlockState state = chunk.getBlockState(pos);

		if (!IHivePlacement.isTreeBlock(state)) {
			return null;
		}

		while (pos.getY() > minBuildHeight) {
			do {
				pos.move(Direction.DOWN);
				state = chunk.getBlockState(pos);
			} while (pos.getY() > minBuildHeight && IHivePlacement.isTreeBlock(state));

			if (pos.getY() <= minBuildHeight || !canReplace(state, level, pos)) {
				return null;
			}

			BlockPos posBelow = pos.below();
			BlockState stateBelow = chunk.getBlockState(posBelow);
			if (canReplace(stateBelow, level, posBelow)) {
				return pos.immutable();
			}
			if (!IHivePlacement.isTreeBlock(stateBelow)) {
				return null;
			}

			pos.set(posBelow);
			state = stateBelow;
		}

		return null;
	}
}
