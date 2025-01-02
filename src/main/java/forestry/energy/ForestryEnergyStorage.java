package forestry.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import net.minecraftforge.energy.EnergyStorage;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;

public class ForestryEnergyStorage extends EnergyStorage implements IStreamable, INbtReadable, INbtWritable {
	public ForestryEnergyStorage(int maxTransfer, int capacity) {
		this(maxTransfer, capacity, EnergyTransferMode.RECEIVE);
	}

	public ForestryEnergyStorage(int maxTransfer, int capacity, EnergyTransferMode mode) {
		super(
				EnergyHelper.scaleForDifficulty(capacity),
				mode.canReceive() ? EnergyHelper.scaleForDifficulty(maxTransfer) : 0,
				mode.canExtract() ? EnergyHelper.scaleForDifficulty(maxTransfer) : 0
		);
	}

	@Override
	public void read(CompoundTag nbt) {
		setEnergyStored(nbt.getInt("Energy"));
	}

	@Override
	public CompoundTag write(CompoundTag nbt) {
		nbt.putInt("Energy", energy);
		return nbt;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeVarInt(this.energy);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		int energyStored = data.readVarInt();
		setEnergyStored(energyStored);
	}

	public int getMaxEnergyReceived() {
		return this.maxReceive;
	}

	/**
	 * Drains an amount of energy, due to decay from lack of work or other factors
	 */
	public void drainEnergy(int amount) {
		setEnergyStored(energy - amount);
	}

	public void generateEnergy(int amount) {
		setEnergyStored(energy + amount);
	}

	// Used by engines for chaining
	public int forceReceiveEnergy(int maxReceive, boolean simulate) {
		int energyReceived = Math.min(capacity - energy, maxReceive);
		if (!simulate) {
			energy += energyReceived;
		}
		return energyReceived;
	}

	public void setEnergyStored(int energyStored) {
		this.energy = energyStored;
		if (this.energy > capacity) {
			this.energy = capacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}

	public int calculateRedstone() {
		return Mth.floor(((float) energy / (float) capacity) * 14.0F) + (energy > 0 ? 1 : 0);
	}
}
