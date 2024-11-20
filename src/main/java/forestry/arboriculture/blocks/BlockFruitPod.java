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
package forestry.arboriculture.blocks;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;

import forestry.arboriculture.tiles.TileFruitPod;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;

public class BlockFruitPod extends CocoaBlock implements EntityBlock {
	private final ForestryPodType podType;

	public BlockFruitPod(ForestryPodType podType) {
		super(BlockSapling.Properties.of().randomTicks().strength(0.2f, 3.0f).sound(SoundType.WOOD));
		this.podType = podType;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		TileFruitPod tile = TileUtil.getTile(level, pos, TileFruitPod.class);
		if (tile == null) {
			return ItemStack.EMPTY;
		}
		return tile.getPickBlock();
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		if (!canSurvive(state, level, pos)) {
			dropResources(state, level, pos);
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			return;
		}

		TileFruitPod tile = TileUtil.getTile(level, pos, TileFruitPod.class);
		if (tile == null) {
			return;
		}

		tile.onBlockTick(rand);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder context) {
		BlockPos pos = BlockUtil.getPos(context);

		if (context.getLevel().getBlockEntity(pos) instanceof TileFruitPod pod) {
			return pod.getDrops();
		} else if (context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TileFruitPod pod) {
			return pod.getDrops();
		}

		return List.of();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		return BlockUtil.isValidPodLocation(level, pos, facing, podType.getFruit().getLogTag());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileFruitPod(pos, state);
	}

	/* IGrowable */
	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
		TileFruitPod podTile = TileUtil.getTile(level, pos, TileFruitPod.class);
		return podTile != null && podTile.canMature();
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state) {
		TileFruitPod podTile = TileUtil.getTile(level, pos, TileFruitPod.class);
		if (podTile != null) {
			podTile.addRipeness(0.5f);
		}
	}
}
