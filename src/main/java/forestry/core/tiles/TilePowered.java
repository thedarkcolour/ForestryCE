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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TilePowered extends TileBase implements IRenderableTile, ISpeedUpgradable, IStreamableGui, IPowerHandler {
	private static final int STEP_INTERVAL = 5;

	private final ForestryEnergyStorage energyStorage;

	// The amount of "ticks" into the current work cycle. Between 0 and ticksPerWorkCycle
	private int stepCounter;
	// The number of "ticks" a work cycle takes to complete. In reality, a "tick" here is 5 real ticks
	private int stepsPerWorkCycle;
	// The amount of energy consumed over the course of an entire work cycle
	private int energyPerWorkCycle;

	protected float speedMultiplier = 1.0f;
	protected float powerMultiplier = 1.0f;

	// the number of work ticks that this tile has had no power
	private int noPowerTime = 0;

	protected TilePowered(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxTransfer, int capacity) {
		super(type, pos, state);

		this.energyStorage = new ForestryEnergyStorage(maxTransfer, capacity, EnergyTransferMode.RECEIVE);

		this.stepsPerWorkCycle = 4;
	}

	public ForestryEnergyStorage getEnergyManager() {
		return this.energyStorage;
	}

	public int getStepCounter() {
		return this.stepCounter;
	}

	// A step is actually 5 ticks. Yay!
	public void setStepsPerWorkCycle(int stepsPerWorkCycle) {
		this.stepsPerWorkCycle = stepsPerWorkCycle;
		this.stepCounter = 0;
	}

	public int getStepsPerWorkCycle() {
		if (this.level.isClientSide) {
			return this.stepsPerWorkCycle;
		}
		return Math.round(this.stepsPerWorkCycle / this.speedMultiplier);
	}

	// RF/t is energyPerWorkCycle / ticksPerWorkCycle
	public void setEnergyPerWorkCycle(int energyPerWorkCycle) {
		this.energyPerWorkCycle = energyPerWorkCycle;
	}

	public int getEnergyPerWorkCycle() {
		return Math.round(this.energyPerWorkCycle * this.powerMultiplier);
	}

	/**
	 * Called every step to determine whether the tile can start working or continue working
	 *
	 * @return Whether this tile can start working or continue working
	 */
	public abstract boolean hasWork();

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		// A step is 5 ticks
		if (!updateOnInterval(STEP_INTERVAL)) {
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

		int stepsPerWorkCycle = getStepsPerWorkCycle();

		if (this.stepCounter < stepsPerWorkCycle) {
			int energyPerWorkCycle = getEnergyPerWorkCycle();
			boolean consumedEnergy = EnergyHelper.consumeEnergyToDoWork(this.energyStorage, stepsPerWorkCycle, energyPerWorkCycle);
			if (consumedEnergy) {
				errorLogic.setCondition(false, ForestryError.NO_POWER);
				this.stepCounter++;
				this.noPowerTime = 0;
			} else {
				this.noPowerTime++;
				if (this.noPowerTime > 4) {
					errorLogic.setCondition(true, ForestryError.NO_POWER);
				}
			}
		}

		if (this.stepCounter >= stepsPerWorkCycle) {
			if (workCycle()) {
				this.stepCounter = 0;
			}
		}
	}

	/**
	 * Called on the server when the tile reaches the end of a work cycle. Consume inputs and produce outputs here.
	 *
	 * @return Whether the work cycle completed successfully. If {@code false}, the machine will call this again every step until it returns {@code true}.
	 */
	protected abstract boolean workCycle();

	/**
	 * Returns the width for a progress bar.
	 *
	 * @param pixels the full width of the progress bar.
	 * @return The number of pixels of the progress bar to draw.
	 */
	public int getProgressScaled(int pixels) {
		int ticksPerWorkCycle = getStepsPerWorkCycle();
		if (ticksPerWorkCycle == 0) {
			return 0;
		}

		return this.stepCounter * pixels / ticksPerWorkCycle;
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.energyStorage.write(nbt, registries);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.energyStorage.read(nbt, registries);
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf buffer) {
		this.energyStorage.writeData(buffer);
		buffer.writeVarInt(this.stepCounter);
		buffer.writeVarInt(getStepsPerWorkCycle());
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf buffer) {
		this.energyStorage.readData(buffer);
		this.stepCounter = buffer.readVarInt();
		this.stepsPerWorkCycle = buffer.readVarInt();
	}

	/* ISpeedUpgradable */
	@Override
	public void applySpeedUpgrade(float speedChange, float powerChange) {
		this.speedMultiplier += speedChange;
		this.powerMultiplier += powerChange;
		this.stepCounter = 0;
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
}
