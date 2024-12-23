/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.genetics.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.IPlantable;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;

public class FertileBeeEffect extends ThrottledBeeEffect {
	private static final int MAX_BLOCK_FIND_TRIES = 5;

	public FertileBeeEffect() {
		super(false, 6, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {

		Level level = housing.getWorldObj();
		BlockPos housingCoordinates = housing.getCoordinates();
		Vec3i area = Bee.getParticleArea(genome, housing);

		int blockX = getRandomOffset(level.random, housingCoordinates.getX(), area.getX());
		int blockZ = getRandomOffset(level.random, housingCoordinates.getZ(), area.getZ());
		int blockMaxY = housingCoordinates.getY() + area.getY() / 2 + 1;
		int blockMinY = housingCoordinates.getY() - area.getY() / 2 - 1;

		for (int attempt = 0; attempt < MAX_BLOCK_FIND_TRIES; ++attempt) {
			if (level.getChunkSource().getChunkNow(blockX >> 4, blockZ >> 4) != null) {
				if (tryTickColumn(level, blockX, blockZ, blockMaxY, blockMinY)) {
					break;
				}
				blockX = getRandomOffset(level.random, housingCoordinates.getX(), area.getX());
				blockZ = getRandomOffset(level.random, housingCoordinates.getZ(), area.getZ());
			}
		}

		return storedData;
	}

	private static int getRandomOffset(RandomSource random, int centrePos, int offset) {
		return centrePos + random.nextInt(offset) - offset / 2;
	}

	private static boolean tryTickColumn(Level level, int x, int z, int maxY, int minY) {
		for (int y = maxY; y >= minY; --y) {
			BlockState state = level.getBlockState(new BlockPos(x, y, z));
			Block block = state.getBlock();
			if (block.isRandomlyTicking(state) && (block instanceof BonemealableBlock || block instanceof IPlantable)) {
				level.scheduleTick(new BlockPos(x, y, z), block, 5);
				return true;
			}
		}
		return false;
	}

}
