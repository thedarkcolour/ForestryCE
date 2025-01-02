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
package forestry.farming.logic.farmables;

import com.google.common.collect.ImmutableSet;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.crops.CropDestroy;

public class FarmableSapling implements IFarmable {
	protected final Item germling;
	protected final Block saplingBlock;
	protected final ImmutableSet<Item> windfall;

	public FarmableSapling(Item germling, ImmutableSet<Item> windfall) {
		this.germling = germling;
		this.windfall = windfall;
		this.saplingBlock = Block.byItem(germling);
	}

	@Override
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		ItemStack copy = germling.copy();
		player.setItemInHand(InteractionHand.MAIN_HAND, copy);
		BlockHitResult result = new BlockHitResult(Vec3.ZERO, Direction.UP, pos.below(), true);    //TODO isInside
		InteractionResult actionResult = copy.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, result));
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		if (actionResult.consumesAction()) {
			Block block = Block.byItem(germling.getItem());
			BlockUtil.sendPlaceSound(level, pos, block == Blocks.AIR ? Blocks.OAK_SAPLING.defaultBlockState() : block.defaultBlockState());
			return true;
		}
		return false;
	}

	@Override
	public boolean isSaplingAt(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() == this.saplingBlock;
	}

	@Override
	public ICrop getCropAt(Level level, BlockPos pos, BlockState state) {
		if (!state.is(BlockTags.LOGS)) {
			return null;
		}

		return new CropDestroy(level, state, pos, null);
	}

	@Override
	public boolean isGermling(ItemStack stack) {
		return stack.is(this.germling);
	}

	@Override
	public void addGermlings(Consumer<ItemStack> accumulator) {
		accumulator.accept(new ItemStack(this.germling));
	}

	@Override
	public void addProducts(Consumer<ItemStack> accumulator) {
		for (Item item : this.windfall) {
			accumulator.accept(new ItemStack(item));
		}
	}

	@Override
	public boolean isWindfall(ItemStack stack) {
		return this.windfall.contains(stack.getItem());
	}
}
