/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.genetics.IGenome;

public interface ITreeGenData {
	int getGirth(IGenome genome);

	float getHeightModifier(IGenome genome);

	/**
	 * @return Position that this tree can grow, or {@code null} if it cannot grow. May be different from pos if there are multiple saplings.
	 */
	@Nullable
	BlockPos getGrowthPos(IGenome genome, LevelAccessor level, BlockPos pos, int expectedGirth, int expectedHeight);

	/**
	 * Places a leaf block for this species with genetic data included.
	 *
	 * @param genome             The genome to use for the leaves.
	 * @param level              The level.
	 * @param pos                The position to set the leaves at.
	 * @param random             Random number generation.
	 * @param convertBlockEntity If {@code true}, ALWAYS use a block entity even if a "default" block can be used.
	 * @return {@code true} if the leaf block was placed.
	 */
	boolean setLeaves(IGenome genome, LevelAccessor level, BlockPos pos, RandomSource random, boolean convertBlockEntity);

	boolean setLogBlock(IGenome genome, LevelAccessor level, BlockPos pos, Direction facing);

	boolean allowsFruitBlocks(IGenome genome);

	boolean trySpawnFruitBlock(LevelAccessor level, RandomSource rand, BlockPos pos);

	IGenome getDefaultGenome();
}
