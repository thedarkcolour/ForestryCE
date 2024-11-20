/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import com.mojang.authlib.GameProfile;

import forestry.api.core.IBlockSubtype;
import forestry.api.genetics.IGenome;

public interface IWoodType extends IBlockSubtype {
	float getHardness();

	String toString();

	/**
	 * Sets a leaf block when the genome is known to be the default genome of the species.
	 *
	 * @param level  The world. (Could be during world generation, so be careful about thread deadlock)
	 * @param pos    The position to set the leaf at.
	 * @param genome The default genome of the species.
	 * @param rand   The random. (Could be world generation random, so prefer this over {@link LevelAccessor#getRandom})
	 * @param owner  If this tree was planted by a player, then this is the player who planted the tree. {@code null} otherwise.
	 * @return Whether the placement was successful. Typically the same return value as {@link LevelAccessor#setBlock}.
	 */
	boolean setDefaultLeaves(LevelAccessor level, BlockPos pos, IGenome genome, RandomSource rand, @Nullable GameProfile owner);

	default SoundEvent getFenceGateOpenSound() {
		return SoundEvents.FENCE_GATE_OPEN;
	}

	default SoundEvent getFenceGateCloseSound() {
		return SoundEvents.FENCE_GATE_CLOSE;
	}
}
