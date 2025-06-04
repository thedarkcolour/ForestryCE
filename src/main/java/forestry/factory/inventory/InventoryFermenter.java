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

import forestry.api.fuels.FuelManager;
import forestry.core.fluids.FluidHelper;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.utils.RecipeUtil;
import forestry.factory.tiles.TileFermenter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Optional;

public class InventoryFermenter extends InventoryAdapterTile<TileFermenter> {
	public static final short SLOT_RESOURCE = 0;
	public static final short SLOT_FUEL = 1;
	public static final short SLOT_CAN_OUTPUT = 2;
	public static final short SLOT_CAN_INPUT = 3;
	public static final short SLOT_INPUT = 4;

	public InventoryFermenter(TileFermenter fermenter) {
		super(fermenter, 5, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex == SLOT_RESOURCE) {
			return RecipeUtil.isFermenterInput(this.tile.getLevel().getRecipeManager(), stack);
		} else if (slotIndex == SLOT_INPUT) {
			Optional<FluidStack> fluid = FluidUtil.getFluidContained(stack);
			return fluid.map(f -> this.tile.getTankManager().canFillFluidType(f)).orElse(false);
		} else if (slotIndex == SLOT_CAN_INPUT) {
			return FluidHelper.isFillableContainerWithRoom(stack);
		} else if (slotIndex == SLOT_FUEL) {
			return FuelManager.fermenterFuel.containsKey(stack);
		}
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return slotIndex == SLOT_CAN_OUTPUT;
	}
}
