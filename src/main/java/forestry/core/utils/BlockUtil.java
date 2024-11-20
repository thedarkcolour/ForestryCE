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
package forestry.core.utils;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import forestry.core.tiles.TileUtil;

public abstract class BlockUtil {
	public static final BlockBehaviour.StatePredicate ALWAYS = (state, level, pos) -> true;
	public static final BlockBehaviour.StatePredicate NEVER = (state, level, pos) -> false;
	public static final BlockBehaviour.StateArgumentPredicate<EntityType<?>> IS_PARROT_OR_OCELOT = (a, b, c, entityType) -> entityType == EntityType.OCELOT || entityType == EntityType.PARROT;

	public static List<ItemStack> getBlockDrops(LevelAccessor level, BlockPos pos) {
		//TODO - this call needs sorting
		return Block.getDrops(level.getBlockState(pos), (ServerLevel) level, pos, TileUtil.getTile(level, pos));

	}

	public static boolean tryPlantCocoaPod(LevelAccessor world, BlockPos pos) {
		Direction facing = getValidPodFacing(world, pos, BlockTags.JUNGLE_LOGS);
		if (facing == null) {
			return false;
		}

		BlockState state = Blocks.COCOA.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
		world.setBlock(pos, state, 18);
		return true;
	}

	@Nullable
	public static Direction getValidPodFacing(LevelAccessor world, BlockPos pos, TagKey<Block> logTag) {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			if (isValidPodLocation(world, pos, facing, logTag)) {
				return facing;
			}
		}
		return null;
	}

	public static boolean isValidPodLocation(LevelReader world, BlockPos pos, Direction direction, TagKey<Block> logTag) {
		pos = pos.relative(direction);
		if (!world.hasChunkAt(pos)) {
			return false;
		}
		BlockState state = world.getBlockState(pos);
		return state.is(logTag);
	}

	public static boolean isBreakableBlock(Level world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		return isBreakableBlock(blockState, world, pos);
	}

	public static boolean isBreakableBlock(BlockState blockState, Level world, BlockPos pos) {
		return blockState.getDestroySpeed(world, pos) >= 0.0F;
	}

	public static boolean isReplaceableBlock(BlockState blockState, Level world, BlockPos pos) {
		Block block = blockState.getBlock();
		return world.getBlockState(pos).canBeReplaced() && true;//!(block instanceof BlockStaticLiquid);
	}

	/* CHUNKS */

	public static boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
		return world.getBlockState(pos).canBeReplaced() && !blockState.liquid();
	}

	public static boolean canPlaceTree(BlockState state, LevelAccessor world, BlockPos pos) {
		BlockPos downPos = pos.below();
		BlockState belowState = world.getBlockState(downPos);
		return !(world.getBlockState(pos).canBeReplaced() && state.liquid()) &&
				!belowState.is(BlockTags.LEAVES) &&
				!belowState.is(BlockTags.LOGS);
	}

	public static BlockPos getNextReplaceableUpPos(Level world, BlockPos pos) {
		BlockPos topPos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pos);
		final BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();
		BlockState blockState = world.getBlockState(newPos.set(pos));

		while (!BlockUtil.canReplace(blockState, world, newPos)) {
			newPos.move(Direction.UP);
			if (newPos.getY() > topPos.getY()) {
				return null;
			}
			blockState = world.getBlockState(newPos);
		}

		return newPos.below();
	}

	@Nullable
	public static BlockPos getNextSolidDownPos(Level world, BlockPos pos) {
		final BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();

		BlockState blockState = world.getBlockState(newPos.set(pos));
		while (canReplace(blockState, world, newPos)) {
			newPos.move(Direction.DOWN);
			if (newPos.getY() <= 0) {
				return null;
			}
			blockState = world.getBlockState(newPos);
		}
		return newPos.above();
	}


	public static boolean setBlockWithPlaceSound(Level level, BlockPos pos, BlockState state) {
		if (level.setBlockAndUpdate(pos, state)) {
			BlockUtil.sendPlaceSound(level, pos, state);
			return true;
		}
		return false;
	}

	public static void setBlockWithBreakSound(Level level, BlockPos pos, BlockState blockState, BlockState oldState) {
		if (level.setBlockAndUpdate(pos, blockState)) {
			sendDestroyEffects(level, pos, oldState);
		}
	}

	public static void setBlockToAirWithSound(Level level, BlockPos pos, BlockState oldState) {
		if (level.removeBlock(pos, false)) {
			sendDestroyEffects(level, pos, oldState);
		}
	}

	// Tells the client to play the block's destroy sound and spawn destroy particles
	public static void sendDestroyEffects(Level level, BlockPos pos, BlockState state) {
		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
	}

	public static void sendPlaceSound(Level level, BlockPos pos, BlockState state) {
		var soundType = state.getSoundType();
		level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.volume + 1.0f) / 2.0f, soundType.pitch * 0.8f);
	}

	public static BlockPos getPos(LootParams.Builder context) {
		Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
		return origin != null ? BlockPos.containing(origin) : BlockPos.ZERO;
	}
}
