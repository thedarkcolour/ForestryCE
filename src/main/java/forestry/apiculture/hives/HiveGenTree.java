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
package forestry.apiculture.hives;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import forestry.api.apiculture.hives.IHiveGen;
import forestry.core.utils.BlockUtil;

public enum HiveGenTree implements IHiveGen {
	INSTANCE;

	@Override
	public boolean canReplace(BlockState blockState, WorldGenLevel world, BlockPos pos) {
		return BlockUtil.canReplace(blockState, world, pos);
	}

	@Override
	public boolean isValidLocation(WorldGenLevel world, BlockPos pos) {
		BlockPos posAbove = pos.above();
		BlockState blockStateAbove = world.getBlockState(posAbove);
		if (!IHiveGen.isTreeBlock(blockStateAbove)) {
			return false;
		}

		// not a good location if right on top of something
		BlockPos posBelow = pos.below();
		BlockState blockStateBelow = world.getBlockState(posBelow);
		return canReplace(blockStateBelow, world, posBelow);
	}

	@Override
	@Deprecated
	public BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ) {
		ChunkAccess chunk = level.getChunk(posX >> 4, posZ >> 4);

		// get top leaf block
		int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, posX & 0xFF, posZ & 0xFF) - 1;

		if (height <= chunk.getMinBuildHeight()) {
			return null;
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, height, posZ);
		BlockState state = chunk.getBlockState(pos);

		if (!IHiveGen.isTreeBlock(state)) {
			return null;
		}

		// get to the bottom of the leaves
		do {
			pos.move(Direction.DOWN);
			state = chunk.getBlockState(pos);
		} while (IHiveGen.isTreeBlock(state));

		return pos.immutable();
	}
}
