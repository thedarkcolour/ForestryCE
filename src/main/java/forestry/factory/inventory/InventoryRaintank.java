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
package forestry.factory.inventory;

import forestry.core.inventory.InventoryAdapterTile;
import forestry.factory.tiles.TileRaintank;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class InventoryRaintank extends InventoryAdapterTile<TileRaintank> {
	public static final short SLOT_RESOURCE = 0;
	public static final short SLOT_PRODUCT = 1;

	public InventoryRaintank(TileRaintank raintank) {
		super(raintank, 3, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_RESOURCE) {
			LazyOptional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(stack);
			return fluidHandler.map(handler -> handler.fill(new FluidStack(Fluids.WATER, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE) > 0).orElse(false);
		}
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return slotIndex == SLOT_PRODUCT;
	}
}
