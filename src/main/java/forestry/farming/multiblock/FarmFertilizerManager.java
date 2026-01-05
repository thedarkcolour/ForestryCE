package forestry.farming.multiblock;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import forestry.cultivation.IFarmHousingInternal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class FarmFertilizerManager implements INbtWritable, INbtReadable, IStreamable {
	private static final int BUFFER_FERTILIZER = 200;
	private final IFarmInventoryInternal inventory;
	private int storedFertilizer;

	public FarmFertilizerManager(IFarmHousingInternal housing) {
		this.inventory = housing.getFarmInventory();
        this.storedFertilizer = 0;
	}

	public boolean hasFertilizer(int amount) {
		if (this.inventory.getFertilizerValue() < 0) {
			return true;
		}

		return this.storedFertilizer >= amount;
	}

	public void removeFertilizer(int amount) {
		if (this.inventory.getFertilizerValue() < 0) {
			return;
		}

        this.storedFertilizer -= amount;
		if (this.storedFertilizer < 0) {
            this.storedFertilizer = 0;
		}
	}

	public boolean maintainFertilizer() {
		if (this.storedFertilizer <= BUFFER_FERTILIZER) {
			int fertilizerValue = this.inventory.getFertilizerValue();
			if (fertilizerValue < 0) {
                this.storedFertilizer += 2000;
			} else if (this.inventory.useFertilizer()) {
                this.storedFertilizer += fertilizerValue;
			}
		}

		return this.storedFertilizer > 0;
	}

	@Override
	public void read(CompoundTag data) {
        this.storedFertilizer = data.getInt("StoredFertilizer");
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		data.putInt("StoredFertilizer", this.storedFertilizer);
		return data;
	}

	public int getStoredFertilizerScaled(IFarmInventoryInternal inventory, int scale) {
		if (this.storedFertilizer == 0) {
			return 0;
		}

		return this.storedFertilizer * scale / (inventory.getFertilizerValue() + BUFFER_FERTILIZER);
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeVarInt(this.storedFertilizer);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
        this.storedFertilizer = data.readVarInt();
	}
}
