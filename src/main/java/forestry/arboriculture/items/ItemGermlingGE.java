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
package forestry.arboriculture.items;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.recipes.IVariableFermentable;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.genetics.ItemGE;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.SpeciesUtil;
import forestry.core.utils.TreeUtil;

public class ItemGermlingGE extends ItemGE implements IVariableFermentable, IColoredItem {
	public ItemGermlingGE(TreeLifeStage type) {
		super(new Item.Properties(), type);
	}

	@Override
	protected ITreeSpecies getSpecies(ItemStack stack) {
		return IIndividualHandlerItem.getSpecies(stack, SpeciesUtil.TREE_TYPE.get());
	}

	@Override
	protected ISpeciesType<?, ?> getType() {
		return SpeciesUtil.TREE_TYPE.get();
	}

	@Override
	public int getColorFromItemStack(ItemStack itemstack, int renderPass) {
		return getSpecies(itemstack).getGermlingColor(this.stage, renderPass);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		BlockHitResult traceResult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.ANY);
		ItemStack stack = playerIn.getItemInHand(handIn);

		if (traceResult.getType() == HitResult.Type.BLOCK) {
			IIndividual tree = IIndividualHandlerItem.getIndividual(stack);

			if (tree != null) {
				BlockPos pos = traceResult.getBlockPos();

				if (this.stage == TreeLifeStage.SAPLING) {
					BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(playerIn, handIn, traceResult));

					return onItemRightClickSapling(stack, worldIn, playerIn, pos, (ITree) tree, context);
				} else if (this.stage == TreeLifeStage.POLLEN) {
					return onItemRightClickPollen(stack, worldIn, playerIn, pos, (ITree) tree);
				}
			}
		}

		return InteractionResultHolder.pass(stack);
	}

	private static InteractionResultHolder<ItemStack> onItemRightClickPollen(ItemStack stack, Level level, Player player, BlockPos pos, ITree pollen) {
		if (!TreeUtil.canMate(TreeUtil.getTreeSafe(level, pos), pollen)) {
			return InteractionResultHolder.pass(stack);
		}

		TileLeaves leaves = TreeUtil.getOrCreateLeaves(level, pos, true);
		if (leaves == null || !TreeUtil.canMate(leaves.getTree(), pollen)) {
			return InteractionResultHolder.pass(stack);
		}

		if (!level.isClientSide) {
			leaves.setMate(pollen);

			BlockUtil.sendDestroyEffects(level, pos, level.getBlockState(pos));

			if (!player.isCreative()) {
				stack.shrink(1);
			}

			return InteractionResultHolder.consume(stack);
		}

		return InteractionResultHolder.success(stack);
	}

	private static InteractionResultHolder<ItemStack> onItemRightClickSapling(ItemStack stack, Level worldIn, Player player, BlockPos pos, ITree tree, BlockPlaceContext context) {
		// x, y, z are the coordinates of the block "hit", can thus either be the soil or tall grass, etc.
		BlockState hitBlock = worldIn.getBlockState(pos);
		if (!hitBlock.canBeReplaced(context)) {
			if (!worldIn.isEmptyBlock(pos.above())) {
				return InteractionResultHolder.pass(stack);
			}
			pos = pos.above();
		}

		if (tree.canStay(worldIn, pos)) {
			if (SpeciesUtil.TREE_TYPE.get().plantSapling(worldIn, tree, player.getGameProfile(), pos)) {
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				return InteractionResultHolder.success(stack);
			}
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public float getFermentationModifier(ItemStack stack) {
		IIndividual tree = IIndividualHandlerItem.getIndividual(stack);
		if (tree == null) {
			return 1.0f;
		}
		return tree.getGenome().getActiveValue(TreeChromosomes.SAPPINESS) * 10;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return 100;
	}
}
