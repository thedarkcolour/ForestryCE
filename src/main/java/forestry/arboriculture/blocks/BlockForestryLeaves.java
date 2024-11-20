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

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import forestry.api.ForestryTags;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.SpeciesUtil;

public class BlockForestryLeaves extends BlockAbstractLeaves implements BonemealableBlock, EntityBlock {
	@Override
	protected ITree getTree(BlockGetter world, BlockPos pos) {
		TileLeaves leaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leaves != null) {
			return leaves.getTree();
		}

		return null;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		super.randomTick(state, world, pos, rand);

		TileLeaves tileLeaves = TileUtil.getTile(world, pos, TileLeaves.class);

		// check leaves tile because they might have decayed
		if (tileLeaves != null && !tileLeaves.isRemoved() && rand.nextFloat() <= 0.1) {
			tileLeaves.onBlockTick(world, pos, state, rand);
		}
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(PERSISTENT);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileLeaves(pos, state);
	}

	@Override
	protected void getLeafDrop(List<ItemStack> drops, Level level, @Nullable BlockPos pos, @Nullable GameProfile profile, float saplingModifier, int fortune, LootParams.Builder context) {
		if (!(level.getBlockEntity(pos) instanceof TileLeaves leaves)) {
			return;
		}

		ITree tree = leaves.getTree();
		if (tree == null) {
			return;
		}

		// Add saplings
		List<ITree> saplings = tree.getSaplings(level, pos, profile, saplingModifier);

		for (ITree sapling : saplings) {
			if (sapling != null) {
				drops.add(SpeciesUtil.TREE_TYPE.get().createStack(sapling, TreeLifeStage.SAPLING));
			}
		}

		// Add fruits
		if (leaves.hasFruit()) {
			drops.addAll(tree.produceStacks(level, pos, leaves.getRipeningTime()));
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		TileLeaves leaves = TileUtil.getTile(level, pos, TileLeaves.class);
		if (leaves != null) {
			IButterfly caterpillar = leaves.getCaterpillar();
			ItemStack heldItem = player.getItemInHand(hand);
			ItemStack otherHand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
			if (heldItem.isEmpty() && otherHand.isEmpty()) {
				if (leaves.hasFruit() && leaves.getRipeness() >= 0.9F) {
					BlockUtil.sendDestroyEffects(level, pos, state);
					for (ItemStack fruit : leaves.pickFruit(ItemStack.EMPTY)) {
						ItemHandlerHelper.giveItemToPlayer(player, fruit);
					}
					return InteractionResult.SUCCESS;
				}
			} else if (heldItem.is(ForestryTags.Items.SCOOPS) && caterpillar != null) {
				ItemStack butterfly = SpeciesUtil.BUTTERFLY_TYPE.get().createStack(caterpillar, ButterflyLifeStage.CATERPILLAR);
				ItemStackUtil.dropItemStackAsEntity(butterfly, level, pos.below());
				leaves.setCaterpillar(null);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	/* IGrowable */

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, boolean isClient) {
		TileLeaves leafTile = TileUtil.getTile(world, pos, TileLeaves.class);
		return leafTile != null && leafTile.hasFruit() && leafTile.getRipeness() < 1.0f;
	}

	@Override
	public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state) {
		TileLeaves leafTile = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leafTile != null) {
			leafTile.addRipeness(0.5f);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		if (level != null && pos != null) {
			TileLeaves leaves = TileUtil.getTile(level, pos, TileLeaves.class);
			if (leaves != null) {
				if (tintIndex == BlockAbstractLeaves.FRUIT_COLOR_INDEX) {
					return leaves.getFruitColour();
				} else {
					return leaves.getFoliageColour();
				}
			}
		}
		return FoliageColor.getDefaultColor();
	}
}
