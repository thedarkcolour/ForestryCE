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
package forestry.apiculture.multiblock;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import forestry.api.climate.IClimateControlled;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.api.recipes.IHygroregulatorRecipe;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.gui.ContainerAlvearyHygroregulator;
import forestry.apiculture.inventory.InventoryHygroregulator;
import forestry.core.config.Constants;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.utils.RecipeUtils;

public class TileAlvearyHygroregulator extends TileAlveary implements Container, ILiquidTankTile, IAlvearyComponent.Climatiser<MultiblockLogicAlveary>, IAlvearyComponent.HasInventory {
	private final TankManager tankManager;
	private final FilteredTank liquidTank;
	private final IInventoryAdapter inventory;

	@Nullable
	private IHygroregulatorRecipe currentRecipe;
	// number of ticks the current temperature change lasts for.
	private int heatTicks;

	public TileAlvearyHygroregulator(BlockPos pos, BlockState state) {
		super(BlockAlvearyType.HYGRO, pos, state);

		this.inventory = new InventoryHygroregulator(this);
		this.liquidTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidRecipeFilter.HYGROREGULATOR_INPUT);
		this.tankManager = new TankManager(this, liquidTank);
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return inventory;
	}

	@Override
	public boolean allowsAutomation() {
		return true;
	}

	/* UPDATING */
	@Override
	public void changeClimate(int tickCount, IClimateControlled climateControlled) {
		if (this.heatTicks <= 0) {
			FluidStack fluid = this.liquidTank.getFluid();

			if (!fluid.isEmpty()) {
				this.currentRecipe = RecipeUtils.getHygroRegulatorRecipe(this.level.getRecipeManager(), fluid);

				if (this.currentRecipe != null) {
					this.liquidTank.drainInternal(this.currentRecipe.getInputFluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
					this.heatTicks = 20;
				}
			}
		}

		if (this.heatTicks > 0) {
			this.heatTicks--;
			if (this.currentRecipe != null) {
				climateControlled.addHumidityChange(this.currentRecipe.getHumiditySteps());
				climateControlled.addTemperatureChange(this.currentRecipe.getTemperatureSteps());
			} else {
				this.heatTicks = 0;
			}
		}

		if (tickCount % 20 == 0) {
			// Check if we have suitable items waiting in the item slot
			FluidHelper.drainContainers(this.tankManager, this, 0);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
		tankManager.read(compoundNBT);

		heatTicks = compoundNBT.getInt("TransferTime");

		if (compoundNBT.contains("CurrentLiquid")) {
			FluidStack liquid = FluidStack.loadFluidStackFromNBT(compoundNBT.getCompound("CurrentLiquid"));
			currentRecipe = RecipeUtils.getHygroRegulatorRecipe(RecipeUtils.getRecipeManager(), liquid);
		}
	}


	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
		tankManager.write(compoundNBT);

		compoundNBT.putInt("TransferTime", heatTicks);
		if (currentRecipe != null) {
			CompoundTag subcompound = new CompoundTag();
			currentRecipe.getInputFluid().writeToNBT(subcompound);
			compoundNBT.put("CurrentLiquid", subcompound);
		}
	}

	/* ILIQUIDTANKCONTAINER */
	@Override
	public TankManager getTankManager() {
		return tankManager;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		LazyOptional<T> superCap = super.getCapability(capability, facing);
		if (superCap.isPresent()) {
			return superCap;
		}
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> tankManager).cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAlvearyHygroregulator(windowId, inv, this);
	}
}
