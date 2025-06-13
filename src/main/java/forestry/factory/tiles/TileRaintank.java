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
package forestry.factory.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.config.Constants;
import forestry.core.fluids.*;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TileBase;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.RaintankMenu;
import forestry.factory.inventory.InventoryRaintank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileRaintank extends TileBase implements WorldlyContainer, ILiquidTankTile {
	private static final FluidStack STACK_WATER = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
	private static final FluidStack WATER_PER_UPDATE = new FluidStack(Fluids.WATER, Constants.RAINTANK_AMOUNT_PER_UPDATE);

	private final FilteredTank resourceTank;
	private final TankManager tankManager;
	private final ContainerFiller containerFiller;

	@Nullable
	private Boolean canDumpBelow = null;
	private boolean dumpingFluid = false;

	// client
	private int fillingProgress;

	public TileRaintank(BlockPos pos, BlockState state) {
		super(FactoryTiles.RAIN_TANK.tileType(), pos, state);
		setInternalInventory(new InventoryRaintank(this));

        this.resourceTank = new FilteredTank(Constants.RAINTANK_TANK_CAPACITY).setFilter(FluidTagFilter.WATER);

        this.tankManager = new TankManager(this, this.resourceTank);

        this.containerFiller = new ContainerFiller(this.resourceTank, Constants.RAINTANK_FILLING_TIME, this, InventoryRaintank.SLOT_RESOURCE, InventoryRaintank.SLOT_PRODUCT);
	}

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
        this.tankManager.write(compoundNBT);
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
        this.tankManager.read(compoundNBT);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		super.writeData(buffer);
        this.tankManager.writeData(buffer);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(RegistryFriendlyByteBuf buffer) {
		super.readData(buffer);
        this.tankManager.readData(buffer);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (updateOnInterval(20)) {
			IErrorLogic errorLogic = getErrorLogic();

			Biome biome = level.getBiome(pos).value();
			errorLogic.setCondition(!(biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN), ForestryError.NO_RAIN_BIOME);

			BlockPos posAbove = pos.above();
			boolean hasSky = level.canSeeSkyFromBelowWater(posAbove);
			errorLogic.setCondition(!hasSky, ForestryError.NO_SKY_RAIN_TANK);

			errorLogic.setCondition(!level.isRainingAt(posAbove), ForestryError.NOT_RAINING);

			if (!errorLogic.hasErrors()) {
                this.resourceTank.fillInternal(WATER_PER_UPDATE, IFluidHandler.FluidAction.EXECUTE);
			}

            this.containerFiller.updateServerSide();
		}

		if (this.canDumpBelow == null) {
            this.canDumpBelow = FluidHelper.canAcceptFluid(level, getBlockPos().below(), Direction.UP, STACK_WATER);
		}

		if (this.canDumpBelow) {
			if (this.dumpingFluid || updateOnInterval(20)) {
                this.dumpingFluid = dumpFluidBelow();
			}
		}
	}

	private boolean dumpFluidBelow() {
		if (!this.resourceTank.isEmpty()) {
			LazyOptional<IFluidHandler> fluidCap = FluidUtil.getFluidHandler(this.level, this.worldPosition.below(), Direction.UP);
			if (fluidCap.isPresent()) {
				return !FluidUtil.tryFluidTransfer(fluidCap.orElse(null), this.tankManager, FluidType.BUCKET_VOLUME / 20, true).isEmpty();
			}
		}
		return false;
	}

	public boolean isFilling() {
		return this.fillingProgress > 0;
	}

	public int getFillProgressScaled(int i) {
		return this.fillingProgress * i / Constants.RAINTANK_FILLING_TIME;
	}

	/* SMP GUI */
	public void getGUINetworkData(int i, int j) {
		switch (i) {
			case 0 -> this.fillingProgress = j;
		}
	}

	public void sendGUINetworkData(AbstractContainerMenu container, ContainerListener iCrafting) {
		iCrafting.dataChanged(container, 0, this.containerFiller.getFillingProgress());
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public void onNeighborTileChange(Level level, BlockPos pos, BlockPos neighbor) {
		super.onNeighborTileChange(level, pos, neighbor);

		if (neighbor.equals(pos.below())) {
            this.canDumpBelow = FluidHelper.canAcceptFluid(level, neighbor, Direction.UP, STACK_WATER);
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> {
				if (facing == Direction.DOWN) {
					return new DrainOnlyFluidHandlerWrapper(this.tankManager);
				}
				return this.tankManager;
			}).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new RaintankMenu(windowId, inv, this);
	}
}
