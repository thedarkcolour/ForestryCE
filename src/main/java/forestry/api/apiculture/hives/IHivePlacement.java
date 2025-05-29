package forestry.api.apiculture.hives;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Determines placement conditions for a naturally generated wild beehive.
 */
public interface IHivePlacement {
	/**
	 * @deprecated Use {@link #getPosForHive(WorldGenLevel, RandomSource, int, int)} with a world gen random instead.
	 */
	@Deprecated
	@Nullable
	BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ);

	/**
	 * Determines the position of a hive.
	 *
	 * @param level The level to generate the hive in.
	 * @param rand  The world generation random. Use this instead of the level random.
	 * @param posX  The X coordinate of the position where the hive should be generated.
	 * @param posZ  The Z coordinate of the position where the hive should be generated.
	 * @return The position to place the hive at, or {@code null} if the hive can't generate at the given coordinates.
	 */
	@Nullable
	default BlockPos getPosForHive(WorldGenLevel level, RandomSource rand, int posX, int posZ) {
		return getPosForHive(level, posX, posZ);
	}

	/**
	 * returns true if the hive can be generated at this location.
	 * Used for advanced conditions, like checking that the ground below the hive is a certain type.
	 */
	boolean isValidLocation(WorldGenLevel world, BlockPos pos);

	/**
	 * returns true if the hive can safely replace the block at this location.
	 */
	boolean canReplace(BlockState blockState, WorldGenLevel world, BlockPos pos);

	static boolean isTreeBlock(BlockState state) {
		return state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
	}
}
