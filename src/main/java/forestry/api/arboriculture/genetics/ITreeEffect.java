/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture.genetics;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import forestry.api.apiculture.genetics.IEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.IRegistryAlleleValue;

/**
 * Defines random tick and animation tick logic for leaves.
 * In Forestry, this is only used by Cherry Blossom trees to spawn petal particles.
 */
public interface ITreeEffect extends IEffect, IRegistryAlleleValue {
	/**
	 * Called whenever this leaf block is randomly ticked, like in {@link Block#randomTick}.
	 *
	 * @param genome     The genome of the tree.
	 * @param storedData Effect data stored between invocations. Not persisted to disk.
	 * @param level      The world of the tree.
	 * @param pos        The position of the leaf block.
	 * @return The effect data to use in the next invocation of this method.
	 */
	IEffectData doEffect(IGenome genome, IEffectData storedData, Level level, BlockPos pos);

	/**
	 * Called whenever this leaf block is randomly animation ticked, like in {@link Block#animateTick}.
	 * Only called on the logical client.
	 *
	 * @param genome The genome of the tree.
	 * @param level  The world of the tree. Always clientside.
	 * @param pos    The position of the leaf block.
	 * @param rand   A random source to use for random number generation.
	 */
	void doAnimationEffect(IGenome genome, Level level, BlockPos pos, RandomSource rand);
}
