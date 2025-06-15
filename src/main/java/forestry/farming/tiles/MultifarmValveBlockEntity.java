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

import forestry.core.fluids.ITankManager;
import forestry.core.tiles.ILiquidTankTile;
import forestry.farming.features.FarmingTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class MultifarmValveBlockEntity extends AbstractMultifarmBlockEntity implements ILiquidTankTile {
	public MultifarmValveBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingTiles.VALVE.tileType(), pos, state);
	}

	@Override
	public ITankManager getTankManager() {
		return getMultiblockLogic().getController().getTankManager();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(this::getTankManager).cast();
		}
		return super.getCapability(capability, facing);
	}
}
