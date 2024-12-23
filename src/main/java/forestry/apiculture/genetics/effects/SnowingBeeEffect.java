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
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.render.ParticleRender;
import forestry.core.utils.VecUtil;

public class SnowingBeeEffect extends ThrottledBeeEffect {
	public SnowingBeeEffect() {
		super(false, 20, true, true);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {

		Level level = housing.getWorldObj();

		TemperatureType temp = housing.temperature();

		switch (temp) {
			case HELLISH:
			case HOT:
			case WARM:
				return storedData;
			default:
		}

		Vec3i area = Bee.getModifiedArea(genome, housing);
		Vec3i offset = VecUtil.scale(area, -1 / 2.0f);

		for (int i = 0; i < 1; i++) {

			BlockPos randomPos = VecUtil.getRandomPositionInArea(level.random, area);

			BlockPos posBlock = randomPos.offset(housing.getCoordinates()).offset(offset);

			// Put snow on the ground
			if (level.hasChunkAt(posBlock)) {
				BlockState state = level.getBlockState(posBlock);
				Block block = state.getBlock();
				if (!state.isAir() && block != Blocks.SNOW || !Blocks.SNOW.defaultBlockState().canSurvive(level, posBlock)) {
					continue;
				}

				if (block == Blocks.SNOW) {
					int layers = state.getValue(SnowLayerBlock.LAYERS);
					if (layers < 7) {
						BlockState moreSnow = state.setValue(SnowLayerBlock.LAYERS, layers + 1);
						level.setBlockAndUpdate(posBlock, moreSnow);
					} else {
						level.setBlockAndUpdate(posBlock, Blocks.SNOW.defaultBlockState());
					}
				} else if (block.defaultBlockState().getMaterial().isReplaceable()) {
					level.setBlockAndUpdate(posBlock, Blocks.SNOW.defaultBlockState());
				}
			}
		}

		return storedData;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		if (housing.getWorldObj().random.nextInt(3) == 0) {
			Vec3i area = Bee.getModifiedArea(genome, housing);
			Vec3i offset = VecUtil.scale(area, -0.5F);

			BlockPos coordinates = housing.getCoordinates();
			Level world = housing.getWorldObj();

			BlockPos spawn = VecUtil.getRandomPositionInArea(world.random, area).offset(coordinates).offset(offset);
			ParticleRender.addEntitySnowFX(world, spawn.getX(), spawn.getY(), spawn.getZ());
			return storedData;
		} else {
			return super.doFX(genome, storedData, housing);
		}
	}

}
