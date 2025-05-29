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
package forestry.core.fluids;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public interface ITankManager extends IFluidHandler {
	// Used to send all tanks to the player upon first opening the screen
	void sendAllTanks(AbstractContainerMenu container, ServerPlayer player);

	// Used to send incremental changes in tanks to players who already have the initial state of the tanks
	void broadcastChanges(AbstractContainerMenu container, ServerPlayer players);

	// Used to clean up cached item stacks when a player closes the screen
	void onClosed(AbstractContainerMenu container);

	@Nullable
	IFluidTank getTank(int tankIndex);

	boolean canFillFluidType(FluidStack fluidStack);

	/**
	 * For updating tanks on the client
	 */
	@OnlyIn(Dist.CLIENT)
	void processTankUpdate(int tankIndex, @Nullable FluidStack contents);
}
