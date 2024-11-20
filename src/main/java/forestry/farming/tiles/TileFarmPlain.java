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
package forestry.farming.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.multiblock.IMultiblockController;
import forestry.farming.blocks.FarmBlock;
import forestry.farming.features.FarmingTiles;

public class TileFarmPlain extends TileFarm {
	public TileFarmPlain(BlockPos pos, BlockState state) {
		super(FarmingTiles.PLAIN.tileType(), pos, state);
	}

	@Override
	public void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord) {
		super.onMachineAssembled(multiblockController, minCoord, maxCoord);

		// set band block meta
		int bandY = maxCoord.getY() - 1;
		if (getBlockPos().getY() == bandY) {
			BlockState state = getBlockState();
			this.level.setBlock(this.worldPosition, state.setValue(FarmBlock.BAND, true), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();

		// set band block meta back to normal
		BlockState state = getBlockState();
		this.level.setBlock(this.worldPosition, state.setValue(FarmBlock.BAND, false), Block.UPDATE_CLIENTS);
	}
}
