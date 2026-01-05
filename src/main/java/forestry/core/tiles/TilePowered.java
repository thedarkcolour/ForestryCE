package forestry.core.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.circuits.ISpeedUpgradable;
import forestry.core.network.IStreamableGui;
import forestry.core.render.TankRenderInfo;
import forestry.energy.EnergyHelper;
import forestry.energy.EnergyTransferMode;
import forestry.energy.ForestryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

// todo rename "ticks" to "steps" in 1.21 to clarify they're different than actual ticks
public abstract class TilePowered extends TileBase implements IRenderableTile, ISpeedUpgradable, IStreamableGui, IPowerHandler {
	private static final int WORK_TICK_INTERVAL = 5; // one Forestry work tick happens every WORK_TICK_INTERVAL game ticks

	private final ForestryEnergyStorage energyStorage;
	private final LazyOptional<ForestryEnergyStorage> energyCap;

	// The amount of "ticks" into the current work cycle. Between 0 and ticksPerWorkCycle
	private int workCounter;
	// The number of "ticks" a work cycle takes to complete. In reality, a "tick" here is 5 real ticks
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
		this.energyCap = LazyOptional.of(() -> this.energyStorage);

		this.ticksPerWorkCycle = 4;
	}

	public ForestryEnergyStorage getEnergyManager() {
		return this.energyStorage;
	}

	public int getWorkCounter() {
		return this.workCounter;
	}

	// A "tick" is actually 5 ticks. Yay!
	public void setTicksPerWorkCycle(int ticksPerWorkCycle) {
		this.ticksPerWorkCycle = ticksPerWorkCycle;
		this.workCounter = 0;
	}

	public int getTicksPerWorkCycle() {
		if (this.level.isClientSide) {
			return this.ticksPerWorkCycle;
		}
		return Math.round(this.ticksPerWorkCycle / this.speedMultiplier);
	}

	// RF/t is energyPerWorkCycle / ticksPerWorkCycle
	public void setEnergyPerWorkCycle(int energyPerWorkCycle) {
		this.energyPerWorkCycle = EnergyHelper.scaleForDifficulty(energyPerWorkCycle);
	}

	public int getEnergyPerWorkCycle() {
		return Math.round(this.energyPerWorkCycle * this.powerMultiplier);
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

		if (this.workCounter < ticksPerWorkCycle) {
			int energyPerWorkCycle = getEnergyPerWorkCycle();
			boolean consumedEnergy = EnergyHelper.consumeEnergyToDoWork(this.energyStorage, ticksPerWorkCycle, energyPerWorkCycle);
			if (consumedEnergy) {
				errorLogic.setCondition(false, ForestryError.NO_POWER);
                this.workCounter++;
                this.noPowerTime = 0;
			} else {
                this.noPowerTime++;
				if (this.noPowerTime > 4) {
					errorLogic.setCondition(true, ForestryError.NO_POWER);
				}
			}
		}

		if (this.workCounter >= ticksPerWorkCycle) {
			if (workCycle()) {
                this.workCounter = 0;
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

		return this.workCounter * pixels / ticksPerWorkCycle;
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
        this.energyStorage.write(nbt);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
        this.energyStorage.read(nbt);
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
        this.energyStorage.writeData(data);
		data.writeVarInt(this.workCounter);
		data.writeVarInt(getTicksPerWorkCycle());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
        this.energyStorage.readData(data);
        this.workCounter = data.readVarInt();
        this.ticksPerWorkCycle = data.readVarInt();
	}

	/* ISpeedUpgradable */
	@Override
	public void applySpeedUpgrade(double speedChange, double powerChange) {
        this.speedMultiplier += speedChange;
        this.powerMultiplier += powerChange;
        this.workCounter = 0;
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
		if (!this.remove && capability == ForgeCapabilities.ENERGY) {
			return this.energyCap.cast();
		}
		return super.getCapability(capability, facing);
	}
}
