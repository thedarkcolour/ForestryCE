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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.core.utils.VecUtil;

public class GlacialBeeEffect extends ThrottledBeeEffect {
	public GlacialBeeEffect() {
		super(false, 200, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();

		if (housing.temperature().isWarmerOrEqual(TemperatureType.WARM)) {
			return storedData;
		}

		Vec3i area = genome.getActiveValue(BeeChromosomes.TERRITORY);
		Vec3i offset = VecUtil.scale(area, -1 / 2.0f);
		BlockPos housingCoords = housing.getCoordinates();

		for (int i = 0; i < 10; i++) {

			BlockPos randomPos = VecUtil.getRandomPositionInArea(level.random, area);
			BlockPos posBlock = VecUtil.sum(randomPos, housingCoords, offset);

			// Freeze water
			if (level.hasChunkAt(posBlock)) {
				Block block = level.getBlockState(posBlock).getBlock();
				if (block == Blocks.WATER) {
					if (level.isEmptyBlock(new BlockPos(posBlock.getX(), posBlock.getY() + 1, posBlock.getZ()))) {
						level.setBlockAndUpdate(posBlock, Blocks.ICE.defaultBlockState());
					}
				}
			}
		}

		return storedData;
	}
}
