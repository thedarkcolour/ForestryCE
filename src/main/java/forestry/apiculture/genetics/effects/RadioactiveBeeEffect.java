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

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.core.damage.CoreDamageTypes;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.VecUtil;

public class RadioactiveBeeEffect extends ThrottledBeeEffect {
	public RadioactiveBeeEffect() {
		super(true, 40, false, true);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		harmEntities(genome, housing);

		return destroyEnvironment(genome, storedData, housing);
	}

	private void harmEntities(IGenome genome, IBeeHousing housing) {
		List<LivingEntity> entities = ThrottledBeeEffect.getEntitiesInRange(genome, housing, LivingEntity.class);
		for (LivingEntity entity : entities) {
			int damage = 8;

			// Entities are not attacked if they wear a full set of apiarist's armor.
			int count = BeeManager.armorApiaristHelper.wearsItems(entity, this, true);
			damage -= count * 2;
			if (damage <= 0) {
				continue;
			}

			entity.hurt(CoreDamageTypes.source(housing.getWorldObj(), CoreDamageTypes.RADIOACTIVE), damage);
		}
	}

	private static IEffectData destroyEnvironment(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();
		RandomSource rand = level.random;

		Vec3i area = VecUtil.scale(genome.getActiveValue(BeeChromosomes.TERRITORY), 2);
		Vec3i offset = VecUtil.scale(area, -1 / 2.0f);
		BlockPos posHousing = housing.getCoordinates();

		for (int i = 0; i < 20; i++) {
			BlockPos randomPos = VecUtil.getRandomPositionInArea(rand, area);
			BlockPos posBlock = randomPos.offset(posHousing);
			posBlock = posBlock.offset(offset);

			if (posBlock.getY() <= 1 || posBlock.getY() >= level.getMaxBuildHeight()) {
				continue;
			}

			// Don't destroy ourselves or blocks below us.
			if (posBlock.getX() == posHousing.getX() && posBlock.getZ() == posHousing.getZ() && posBlock.getY() <= posHousing.getY()) {
				continue;
			}

			if (!level.hasChunkAt(posBlock) || level.isEmptyBlock(posBlock)) {
				continue;
			}

			BlockState state = level.getBlockState(posBlock);

			if (state.getBlock() instanceof BlockAlveary) {
				continue;
			}

			BlockEntity tile = TileUtil.getTile(level, posBlock);
			if (tile instanceof IBeeHousing) {
				continue;
			}

			if (state.getDestroySpeed(level, posBlock) < 0) {
				continue;
			}

			BlockUtil.setBlockToAirWithSound(level, posBlock, state);
			break;
		}

		return storedData;
	}
}
