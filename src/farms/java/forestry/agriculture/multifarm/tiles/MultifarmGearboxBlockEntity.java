package forestry.agriculture.multifarm.tiles;

import forestry.agriculture.multifarm.multiblock.FarmPattern;
import forestry.api.core.multiblock.IFarmComponent;
import forestry.api.core.multiblock.IFarmController;
import forestry.core.content.energy.EnergyHelper;
import forestry.core.content.energy.ForestryEnergyStorage;
import forestry.agriculture.features.MultifarmBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class MultifarmGearboxBlockEntity extends MultifarmBlockEntity implements IFarmComponent.Active {
	private static final int WORK_CYCLES = 4;
	private static final int ENERGY_PER_OPERATION = WORK_CYCLES * 50;

	private final ForestryEnergyStorage energyStorage;

	private int activationDelay = 0;
	private int previousDelays = 0;
	private int workCounter;

	public MultifarmGearboxBlockEntity(BlockPos pos, BlockState state) {
		super(MultifarmBlockEntities.GEARBOX.tileType(), pos, state);

		this.energyStorage = new ForestryEnergyStorage(200, 10000);
	}

	/* SAVING & LOADING */
	@Override
	public void loadAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.loadAdditional(compoundNBT, registries);

        this.energyStorage.read(compoundNBT, registries);

        this.activationDelay = compoundNBT.getInt("ActivationDelay");
        this.previousDelays = compoundNBT.getInt("PrevDelays");
	}


	@Override
	public void saveAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.saveAdditional(compoundNBT, registries);

        this.energyStorage.write(compoundNBT, registries);

		compoundNBT.putInt("ActivationDelay", this.activationDelay);
		compoundNBT.putInt("PrevDelays", this.previousDelays);
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

	public IEnergyStorage getEnergyHandler(Direction facing) {
		if (!this.remove) {
			return this.energyStorage;
		}
		return null;
	}

	@Override
	public String patternTypeId() {
		return FarmPattern.GEARBOX;
	}

}
