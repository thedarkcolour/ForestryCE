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
package forestry.core.tiles;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import forestry.api.core.IErrorLogic;
import forestry.core.circuits.ISpeedUpgradable;
import forestry.api.core.ForestryError;
import forestry.core.network.IStreamableGui;
import forestry.core.render.TankRenderInfo;
import forestry.energy.EnergyHelper;
import forestry.energy.ForestryEnergyStorage;
import forestry.energy.EnergyTransferMode;

public abstract class TilePowered extends TileBase implements IRenderableTile, ISpeedUpgradable, IStreamableGui, IPowerHandler {
	private static final int WORK_TICK_INTERVAL = 5; // one Forestry work tick happens every WORK_TICK_INTERVAL game ticks

	private final ForestryEnergyStorage energyStorage;
	private final LazyOptional<ForestryEnergyStorage> energyCap;

	// The amount of ticks into the current work cycle. Between 0 and ticksPerWorkCycle
	private int workCounter;
	// The number of ticks a work cycle takes to complete
	private int ticksPerWorkCycle;
	// The amount of energy consumed over the course of an entire work cycle
	private int energyPerWorkCycle;

	protected float speedMultiplier = 1.0f;
	protected float powerMultiplier = 1.0f;

	// the number of work ticks that this tile has had no power
	private int noPowerTime = 0;

	protected TilePowered(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxTransfer, int capacity) {
		super(type, pos, state);

		this.energyStorage = new ForestryEnergyStorage(maxTransfer, capacity, EnergyTransferMode.RECEIVE);
		this.energyCap = LazyOptional.of(() -> energyStorage);

		this.ticksPerWorkCycle = 4;
	}

	public ForestryEnergyStorage getEnergyManager() {
		return energyStorage;
	}

	public int getWorkCounter() {
		return workCounter;
	}

	public void setTicksPerWorkCycle(int ticksPerWorkCycle) {
		this.ticksPerWorkCycle = ticksPerWorkCycle;
		this.workCounter = 0;
	}

	public int getTicksPerWorkCycle() {
		if (level.isClientSide) {
			return ticksPerWorkCycle;
		}
		return Math.round(ticksPerWorkCycle / speedMultiplier);
	}

	// RF/t is energyPerWorkCycle / ticksPerWorkCycle
	public void setEnergyPerWorkCycle(int energyPerWorkCycle) {
		this.energyPerWorkCycle = EnergyHelper.scaleForDifficulty(energyPerWorkCycle);
	}

	public int getEnergyPerWorkCycle() {
		return Math.round(energyPerWorkCycle * powerMultiplier);
	}

	/* STATE INFORMATION */
	public boolean hasResourcesMin(float percentage) {
		return false;
	}

	public boolean hasFuelMin(float percentage) {
		return false;
	}

	// Called every tick to determine whether the tile can start working or continue working
	public abstract boolean hasWork();

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (!updateOnInterval(WORK_TICK_INTERVAL)) {
			return;
		}

		IErrorLogic errorLogic = getErrorLogic();

		boolean disabled = isRedstoneActivated();
		errorLogic.setCondition(disabled, ForestryError.DISABLED_BY_REDSTONE);
		if (disabled) {
			return;
		}

		if (!hasWork()) {
			return;
		}

		int ticksPerWorkCycle = getTicksPerWorkCycle();

		if (workCounter < ticksPerWorkCycle) {
			int energyPerWorkCycle = getEnergyPerWorkCycle();
			boolean consumedEnergy = EnergyHelper.consumeEnergyToDoWork(energyStorage, ticksPerWorkCycle, energyPerWorkCycle);
			if (consumedEnergy) {
				errorLogic.setCondition(false, ForestryError.NO_POWER);
				workCounter++;
				noPowerTime = 0;
			} else {
				noPowerTime++;
				if (noPowerTime > 4) {
					errorLogic.setCondition(true, ForestryError.NO_POWER);
				}
			}
		}

		if (workCounter >= ticksPerWorkCycle) {
			if (workCycle()) {
				workCounter = 0;
			}
		}
	}

	// Called when the tile reaches the end of a work cycle. Consume inputs and produce outputs here.
	protected abstract boolean workCycle();

	// Returns the width for a progress bar. pixels is the full width of the progress bar.
	public int getProgressScaled(int pixels) {
		int ticksPerWorkCycle = getTicksPerWorkCycle();
		if (ticksPerWorkCycle == 0) {
			return 0;
		}

		return workCounter * pixels / ticksPerWorkCycle;
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		energyStorage.write(nbt);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		energyStorage.read(nbt);
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		energyStorage.writeData(data);
		data.writeVarInt(workCounter);
		data.writeVarInt(getTicksPerWorkCycle());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		energyStorage.readData(data);
		workCounter = data.readVarInt();
		ticksPerWorkCycle = data.readVarInt();
	}

	/* ISpeedUpgradable */
	@Override
	public void applySpeedUpgrade(double speedChange, double powerChange) {
		speedMultiplier += speedChange;
		powerMultiplier += powerChange;
		workCounter = 0;
	}

	/* IRenderableTile */
	@Override
	public TankRenderInfo getResourceTankInfo() {
		return TankRenderInfo.EMPTY;
	}

	@Override
	public TankRenderInfo getProductTankInfo() {
		return TankRenderInfo.EMPTY;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!remove && capability == ForgeCapabilities.ENERGY) {
			return energyCap.cast();
		}
		return super.getCapability(capability, facing);
	}
}
