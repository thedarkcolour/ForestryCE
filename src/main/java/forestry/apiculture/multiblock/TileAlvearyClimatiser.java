package forestry.apiculture.multiblock;

import forestry.api.climate.IClimateControlled;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.core.tiles.IActivatable;
import forestry.energy.EnergyHelper;
import forestry.energy.EnergyTransferMode;
import forestry.energy.ForestryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

// Used by Heater and Fan, which increase and decrease Temperature, respectively
public abstract class TileAlvearyClimatiser extends TileAlveary implements IActivatable, IAlvearyComponent.Climatiser<MultiblockLogicAlveary> {
	private static final int TICKS_PER_CYCLE = 1;
	private static final int FE_PER_OPERATION = 50;

	private final ForestryEnergyStorage energyStorage;
	private final byte temperatureSteps;

	private int workingTime = 0;

	protected TileAlvearyClimatiser(BlockAlveary.Type alvearyType, BlockPos pos, BlockState state, byte temperatureSteps) {
		super(alvearyType, pos, state);
		this.temperatureSteps = temperatureSteps;

		this.energyStorage = new ForestryEnergyStorage(1000, 2000, EnergyTransferMode.RECEIVE);
	}

	/* UPDATING */
	@Override
	public void changeClimate(int tick, IClimateControlled climateControlled) {
		if (this.workingTime < 20 && EnergyHelper.consumeEnergyToDoWork(this.energyStorage, TICKS_PER_CYCLE, FE_PER_OPERATION)) {
			// one tick of work for every 10 RF
			this.workingTime += FE_PER_OPERATION / 10;
		}

		if (this.workingTime > 0) {
			this.workingTime--;
			climateControlled.addTemperatureChange(this.temperatureSteps);
		}

		setActive(this.workingTime > 0);
	}

	/* LOADING & SAVING */
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.energyStorage.read(nbt, registries);
		this.workingTime = nbt.getInt("Heating");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.energyStorage.write(nbt, registries);
		nbt.putInt("Heating", this.workingTime);
	}

	/* Network */
	@Override
	protected void encodeDescriptionPacket(CompoundTag packetData) {
		super.encodeDescriptionPacket(packetData);
	}

	@Override
	protected void decodeDescriptionPacket(CompoundTag packetData) {
		super.decodeDescriptionPacket(packetData);
	}

	/* IActivatable */
	@Override
	public boolean isActive() {
		return getBlockState().getValue(BlockAlveary.STATE) == BlockAlveary.State.ON;
	}

	@Override
	public void setActive(boolean active) {
		if (isActive() != active) {
			this.level.setBlockAndUpdate(this.worldPosition, getBlockState().setValue(BlockAlveary.STATE, active ? BlockAlveary.State.ON : BlockAlveary.State.OFF));
		}
	}
}
