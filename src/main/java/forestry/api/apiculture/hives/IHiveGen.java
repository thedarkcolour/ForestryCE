/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.apiculture.hives;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Determines placement conditions for a naturally generated wild beehive.
 * todo rename to IHivePlacement
 */
public interface IHiveGen {
	/**
	 * @return The position including X, Y, and Z coordinates to place the hive at, or {@code null} if the hive can't
	 * generate at the given posX and posZ coordinates.
	 */
	@Nullable
	BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ);

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
