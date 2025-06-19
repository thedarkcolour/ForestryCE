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

import forestry.api.multiblock.IFarmComponent;
import forestry.api.multiblock.IFarmController;
import forestry.energy.EnergyHelper;
import forestry.energy.ForestryEnergyStorage;
import forestry.farming.features.FarmingTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class MultifarmGearboxBlockEntity extends AbstractMultifarmBlockEntity implements IFarmComponent.Active {
	private static final int WORK_CYCLES = 4;
	private static final int ENERGY_PER_OPERATION = WORK_CYCLES * 50;

	private final ForestryEnergyStorage energyStorage;

	private int activationDelay = 0;
	private int previousDelays = 0;
	private int workCounter;

	public MultifarmGearboxBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingTiles.GEARBOX.tileType(), pos, state);

		this.energyStorage = new ForestryEnergyStorage(200, 10000);
	}

	/* SAVING & LOADING */
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

        this.energyStorage.read(nbt, registries);

        this.activationDelay = nbt.getInt("ActivationDelay");
        this.previousDelays = nbt.getInt("PrevDelays");
	}


	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

        this.energyStorage.write(nbt, registries);

		nbt.putInt("ActivationDelay", this.activationDelay);
		nbt.putInt("PrevDelays", this.previousDelays);
	}

	@Override
	public void updateServer(int tickCount) {
		if (this.energyStorage.getEnergyStored() <= 0) {
			return;
		}

		if (this.activationDelay > 0) {
            this.activationDelay--;
			return;
		}

		// Hard limit to 4 cycles / second.
		if (this.workCounter < WORK_CYCLES && EnergyHelper.consumeEnergyToDoWork(this.energyStorage, WORK_CYCLES, ENERGY_PER_OPERATION)) {
            this.workCounter++;
		}

		if (this.workCounter >= WORK_CYCLES && tickCount % 5 == 0) {
			IFarmController farmController = getMultiblockLogic().getController();
			if (farmController.doWork()) {
                this.workCounter = 0;
                this.previousDelays = 0;
			} else {
				// If the central TE doesn't have work, we add to the activation delay to throttle the CPU usage.
                this.activationDelay = Math.min(10 * this.previousDelays, 120);
                this.previousDelays++; // First delay is free!
			}
		}
	}

	@Override
	public void updateClient(int tickCount) {
		// todo add sided multiblock component ticking and remove this
	}

	public ForestryEnergyStorage getEnergyManager() {
		return this.energyStorage;
	}
}
