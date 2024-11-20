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
package forestry.lepidopterology.entities;

import java.util.EnumSet;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.IPlantable;

public class AIButterflyRest extends AIButterflyBase {
	public AIButterflyRest(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (entity.getExhaustion() < EntityButterfly.EXHAUSTION_REST
				&& entity.canFly()) {
			return false;
		}

		Vec3 entityPos = entity.position();
		int x = (int) entityPos.x;
		int y = (int) Math.floor(entityPos.y);
		int z = (int) entityPos.z;
		BlockPos pos = new BlockPos(x, y, z);

		if (!canLand(pos)) {
			return false;
		}

		Level level = entity.level();
		pos = pos.relative(Direction.DOWN);
		if (level.isEmptyBlock(pos)) {
			return false;
		}
		BlockState blockState = level.getBlockState(pos);
		if (blockState.liquid()) {
			return false;
		}
		if (!entity.getButterfly().isAcceptedEnvironment(level, x, pos.getY(), z)) {
			return false;
		}

		entity.setDestination(null);
		entity.setState(EnumButterflyState.RESTING);
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (entity.getExhaustion() <= 0 && entity.canFly()) {
			return false;
		}
		return !entity.isInWater();
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void tick() {
		entity.changeExhaustion(-1);
	}

	private boolean canLand(BlockPos pos) {
		Level level = entity.level();
		if (!level.hasChunkAt(pos)) {
			return false;
		}
		BlockState blockState = level.getBlockState(pos);
		if (!blockState.isAir()) {
			return false;
		}
		if (isPlant(blockState)) {
			return true;
		}

		BlockState belowState = level.getBlockState(pos.below());
		return isRest(belowState) || belowState.is(BlockTags.LEAVES);
	}

	private static boolean isRest(BlockState state) {
		return state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS);
	}

	private static boolean isPlant(BlockState state) {
		Block block = state.getBlock();
		if (state.is(BlockTags.FLOWERS)) {
			return true;
		} else if (block instanceof IPlantable) {
			return true;
		} else if (block instanceof BonemealableBlock) {
			return true;
		} else {
			return state.is(BlockTags.LEAVES);
		}
	}
}
