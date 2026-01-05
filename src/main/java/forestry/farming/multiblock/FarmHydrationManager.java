package forestry.farming.multiblock;

import forestry.api.core.HumidityType;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.api.core.TemperatureType;
import forestry.core.network.IStreamable;
import forestry.cultivation.IFarmHousingInternal;
import forestry.farming.gui.IFarmLedgerDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class FarmHydrationManager implements IFarmLedgerDelegate, INbtWritable, INbtReadable, IStreamable {
	private static final int DELAY_HYDRATION = 100;
	private static final float RAINFALL_MODIFIER_MAX = 15f;
	private static final float RAINFALL_MODIFIER_MIN = 0.5f;

	private final IFarmHousingInternal housing;
	private int hydrationDelay = 0;
	private int ticksSinceRainfall = 0;

	public FarmHydrationManager(IFarmHousingInternal housing) {
		this.housing = housing;
	}

	public void updateServer() {
		Level world = this.housing.getWorldObj();
		BlockPos coordinates = this.housing.getTopCoord();
		if (world.isRainingAt(coordinates.above())) {
			if (this.hydrationDelay > 0) {
                this.hydrationDelay--;
			} else {
                this.ticksSinceRainfall = 0;
			}
		} else {
            this.hydrationDelay = DELAY_HYDRATION;
			if (this.ticksSinceRainfall < Integer.MAX_VALUE) {
                this.ticksSinceRainfall++;
			}
		}
	}

	@Override
	public float getHydrationModifier() {
		return getHydrationTempModifier() * getHydrationHumidModifier() * getHydrationRainfallModifier();
	}

	@Override
	public float getHydrationTempModifier() {
		return switch (temperature()) {
			case NORMAL -> 1.0f;
			case WARM -> 1.5f;
			case HOT, HELLISH -> 2.0f;
			default -> 0.8f;
		};
	}

	@Override
	public float getHydrationHumidModifier() {
		return switch (humidity()) {
			case ARID -> 2.0f;
			case NORMAL -> 1.5f;
			case DAMP -> 1.0f;
		};
	}

	@Override
	public TemperatureType temperature() {
		return this.housing.temperature();
	}

	@Override
	public HumidityType humidity() {
		return this.housing.humidity();
	}

	@Override
	public float getHydrationRainfallModifier() {
		return Mth.clamp((float) this.ticksSinceRainfall / 24000, RAINFALL_MODIFIER_MIN, RAINFALL_MODIFIER_MAX);
	}

	@Override
	public double getDrought() {
		return Math.round((double) this.ticksSinceRainfall / 24000 * 10) / 10.;
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		compoundNBT.putInt("HydrationDelay", this.hydrationDelay);
		compoundNBT.putInt("TicksSinceRainfall", this.ticksSinceRainfall);
		return compoundNBT;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeVarInt(this.hydrationDelay);
		data.writeVarInt(this.ticksSinceRainfall);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
        this.hydrationDelay = data.readVarInt();
        this.ticksSinceRainfall = data.readVarInt();
	}

	@Override
	public void read(CompoundTag nbt) {
        this.hydrationDelay = nbt.getInt("HydrationDelay");
        this.ticksSinceRainfall = nbt.getInt("TicksSinceRainfall");
	}
}
