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

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.ICrop;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.crops.CropDestroy;

public class FarmableMushroom extends FarmableBase {
	public FarmableMushroom(ItemStack mushroom, BlockState plantedMushroom) {
		super(mushroom, plantedMushroom, Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false), false);
	}

	@Override
	public ICrop getCropAt(Level level, BlockPos pos, BlockState state) {
		Block block = state.getBlock();

		if (block == Blocks.MUSHROOM_STEM || block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK) {
			return new CropDestroy(level, state, pos, null);
		}

		return null;
	}

	@Override
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		BlockState state;

		if (germling.getItem() == Items.BROWN_MUSHROOM) {
			state = Blocks.BROWN_MUSHROOM.defaultBlockState();
		} else if (germling.getItem() == Items.RED_MUSHROOM) {
			state = Blocks.RED_MUSHROOM.defaultBlockState();
		} else {
			return false;
		}

		if (state.canSurvive(level, pos)) {
			return BlockUtil.setBlockWithPlaceSound(level, pos, state);
		} else {
			return false;
		}
	}

	@Override
	public void addProducts(Consumer<ItemStack> accumulator) {
		accumulator.accept(new ItemStack(Items.RED_MUSHROOM));
		accumulator.accept(new ItemStack(Items.BROWN_MUSHROOM));
	}
}
